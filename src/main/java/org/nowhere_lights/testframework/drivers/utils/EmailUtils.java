package org.nowhere_lights.testframework.drivers.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.*;
import javax.mail.search.SubjectTerm;
import javax.net.ssl.*;
import java.io.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.awaitility.Awaitility.await;

public class EmailUtils {

    private static final Logger _logger = LogManager.getLogger(EmailUtils.class.getSimpleName());
    private static final String EMAIL_USERNAME = PropertiesContext.getInstance().getProperty("email.address");
    private Folder folder;

    public enum EmailFolder {
        INBOX("INBOX"),
        SPAM("SPAM");

        private String text;

        private EmailFolder(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    /**
     * Uses email.username and email.password properties from the properties file. Reads from Inbox folder of the email application
     *
     * @throws MessagingException
     */
    public EmailUtils() throws MessagingException {
        this(EmailFolder.INBOX);
    }

    /**
     * Uses username and password in properties file to read from a given folder of the email application
     *
     * @param emailFolder Folder in email application to interact with
     * @throws MessagingException
     */
    private EmailUtils(EmailFolder emailFolder) throws MessagingException {
        this(getEmailUsernameFromProperties(),
                getEmailPasswordFromProperties(),
                getEmailServerFromProperties(),
                emailFolder);
    }

    /**
     * Connects to email server with credentials provided to read from a given folder of the email application
     *
     * @param username    Email username (e.g. janedoe@email.com)
     * @param password    Email password
     * @param server      Email server (e.g. smtp.email.com)
     * @param emailFolder Folder in email application to interact with
     */
    public EmailUtils(String username, String password, String server, EmailFolder emailFolder) throws MessagingException {
        Properties props = System.getProperties();
        try {
            props.load(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("email.properties")));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Session session = Session.getInstance(props);
        Store store = session.getStore("imaps");
        store.connect(server, username, password);


        folder = store.getFolder(emailFolder.getText());
        folder.open(Folder.READ_WRITE);
    }


    //************* GET EMAIL PROPERTIES *******************


    private static String getEmailUsernameFromProperties() {
        return System.getProperty("email.address");
    }

    private static String getEmailPasswordFromProperties() {
        return System.getProperty("email.password");
    }

    private static String getEmailServerFromProperties() {
        return System.getProperty("email.server");
    }


    //************* EMAIL ACTIONS *******************


    private int getNumberOfMessages() throws MessagingException {
        return folder.getMessageCount();
    }


    /**
     * Gets a message by its position in the folder. The earliest message is indexed at 1.
     */
    public Message getMessageByIndex(int index) throws MessagingException {
        return folder.getMessage(index);
    }

    public Message getLatestMessage() throws MessagingException {
        return getMessageByIndex(getNumberOfMessages());
    }

    public int getNumberOfUnreadMessages() throws MessagingException {
        return folder.getUnreadMessageCount();
    }

    public void markUnreadMessagesAsRead() throws MessagingException {
        folder.getMessages(0, 20);
    }

    /**
     * Gets all messages within the folder
     */
    public Message[] getAllMessages() throws MessagingException {
        return folder.getMessages();
    }

    /**
     * @param maxToGet maximum number of messages to get, starting from the latest. For example,
     *                 enter 100 to get the last 100 messages received.
     */
    public Message[] getMessages(int maxToGet) throws MessagingException {
        Map<String, Integer> indices = getStartAndEndIndices(maxToGet);
        return folder.getMessages(indices.get("startIndex"), indices.get("endIndex"));
    }

    /**
     * Searches for messages with a specific subject
     *
     * @param subject     Subject to search messages for
     * @param unreadOnly  Indicate whether to only return matched messages that are unread
     * @param maxToSearch maximum number of messages to search, starting from the latest.
     *                    For example, enter 100 to search through the last 100 messages.
     */
    private List<Message> getMessagesBySubject(String email, String subject, boolean unreadOnly, int maxToSearch, String content) throws Exception {
        Map<String, Integer> indices = getStartAndEndIndices(maxToSearch);

        Message[] messages = folder.search(
                new SubjectTerm(subject),
                folder.getMessages(indices.get("startIndex"), indices.get("endIndex")));

        if (unreadOnly) {
            List<Message> unreadMessages = new ArrayList<>();
            for (Message message : messages) {
                if (isMessageUnread(message) && isMessageForRecipient(message, email) && isTextInMessage(message, content)) {
                    unreadMessages.add(message);
                }
            }
            return unreadMessages;
        }
        return null;

    }

    /**
     * Returns HTML of the email's content
     */
    private String getMessageContent(Message message) throws Exception {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(message.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        return builder.toString();
    }

    /**
     * Returns all urls from an email message with the linkText specified
     */
    private List<String> getUrlsFromMessage(Message message, String linkText) throws Exception {
        String html = getMessageContent(message);
        List<String> allMatches = new ArrayList<>();
        Matcher matcher = Pattern.compile("(<a [^>]+>)" + linkText + "</a>").matcher(html);
        while (matcher.find()) {
            String aTag = matcher.group(1);
            allMatches.add(aTag.substring(aTag.indexOf("https"), aTag.indexOf("\">")));
        }
        return allMatches;
    }

    /**
     * @param emailAddress should be email address that used in the test, not actual
     */
    public String getLinkWithToken(String emailAddress, String subject, String content) throws Exception {
        waitForEmailOrPhoneLinkToBeReceived(emailAddress, subject,
                content);
        Message email = getMessagesBySubject(emailAddress, subject,
                true, 1, content).get(0);
        String formatted = getUrlsFromMessage(email, content).get(0)
                .replaceAll("\\\".*\\\".+$", "");
        _logger.info("'Received link' content:\n" + formatted);
        return formatted;
    }

    public String getCheckingLink(String content) {
        for (String line : content.split("\n")) {
            if (line.trim().contains("https://")) {
                Pattern p = Pattern.compile("https://\"(.*?)\"");
                Matcher m = p.matcher(line);
                if (m.find()) {
                    return m.group(1); // this variable should contain the link URL
                }
            }
        }
        _logger.error("Link couldn't be extracted");
        return null;
    }

    public String getAuthorizationLink(String subject, String content) throws Exception {
        waitForEmailOrPhoneLinkToBeReceived(EMAIL_USERNAME, subject,
                content);
        Message email = getMessagesBySubject(EMAIL_USERNAME, subject,
                true, 1, content).get(0);
        BufferedReader reader = new BufferedReader(new InputStreamReader(email.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(content)) {
                String line1 = line.substring(line.indexOf(": ") + 1);
                return StringUtils.substring(line1, 1);
            }
        }
        return null;
    }

    /**
     * Note: please pay attention, that email address here is the one, where all the emails stored
     * <p>
     * Use this method to get shorten link from received SMS
     *
     * @param content is also a start point that points from which part of email text should be read,
     *                e.g. 'New email: '
     * @param end     points to the end of content, there could be a few of them
     *                e.g.  ' reply to this email or visit Google.'
     */
    public String getAuthoLink(String subject, String content, String... end) throws Exception {
        waitForEmailOrPhoneLinkToBeReceived(EMAIL_USERNAME, subject,
                content);
        Message email = getMessagesBySubject(EMAIL_USERNAME, subject,
                true, 4, content).get(0);

        String html = getMessageContent(email);
        try {
            int n;
            for (n = 0; n < end.length; n++)
                if (end.length == 1)
                    break;

            return StringUtils.substringBetween(html, content, "<")
                    .replace(end[n], "")
                    .replace(end[n], "")
                    .replace(end[n], "");
        } catch (NullPointerException ignored) {
            return null;
        }
    }

    /**
     * @param emailAddress email
     * @param subject      email's subject
     * @param content      a part from email content used to use as keywords
     */
    public String getResetPasswordLink(String emailAddress, String subject, String content) throws Exception {
        String formatted;
        waitForEmailOrPhoneLinkToBeReceived(emailAddress, subject, content);
        Message email = getMessagesBySubject(emailAddress, subject,
                true, 5, content).get(0);
        try {
            formatted = getUrlsFromMessage(email, "Reset Password").get(0)
                    .replaceAll("\\\".*\\\".+$", "")
                    .replace("&amp;", "&");
        } catch (NullPointerException ignored) {
            return null;
        }
        _logger.info("Reset password content: " + formatted);
        return formatted;
    }

    /**
     * @param channelName
     */
    public String getNewBlockLink(String channelName, String subject, String content) throws Exception {
        waitForEmailOrPhoneLinkToBeReceived(EMAIL_USERNAME, subject,
                content);
        Message email = getMessagesBySubject(EMAIL_USERNAME, subject,
                true, 4, content).get(0);
        String innerContent = getMessageContent(email);

        try {
            return StringUtils.substringBetween(innerContent, channelName + " ", " ")
                    .replace("=", "")
                    .replace("</td></tr><tr><td", "");
        } catch (NullPointerException ignored) {
            return null;
        }
    }

    private Map<String, Integer> getStartAndEndIndices(int max) throws MessagingException {
        int endIndex = getNumberOfMessages();
        int startIndex = endIndex - max;

        //In event that maxToGet is greater than number of messages that exist
        if (startIndex < 1) {
            startIndex = 1;
        }

        Map<String, Integer> indices = new HashMap<>();
        indices.put("startIndex", startIndex);
        indices.put("endIndex", endIndex);

        return indices;
    }

    //************* BOOLEAN METHODS *******************

    private boolean isMessageUnread(Message message) throws Exception {
        return !message.isSet(Flags.Flag.SEEN);
    }

    private boolean isMessageForRecipient(Message message, String email) throws Exception {
        Address[] addresses = message.getRecipients(Message.RecipientType.TO);
        for (Address address : addresses) {
            if (address.toString().equals(email))
                return true;
        }
        return false;
    }

    /**
     * Searches an email message for a specific string
     */
    private boolean isTextInMessage(Message message, String content) throws Exception {
        String receivedContent = getMessageContent(message);

        //Some Strings within the email have whitespace and some have break coding. Need to be the same.
        receivedContent = receivedContent.replace("&nbsp;", " ");
        return receivedContent.contains(content);
    }

    private boolean isNewMessageReceived(String email, String subject, String content) throws Exception {
        Map<String, Integer> indices = getStartAndEndIndices(6);

        Message[] messages = folder.search(
                new SubjectTerm(subject),
                folder.getMessages(indices.get("startIndex"), indices.get("endIndex")));

        for (Message message : messages)
            if (isMessageUnread(message) && isMessageForRecipient(message, email) && isTextInMessage(message, content)) {
                message.setFlag(Flags.Flag.SEEN, false);
                return true;
            }
        return false;
    }

    private void waitForEmailOrPhoneLinkToBeReceived(String email, String subject, String content) {
        await()
                .pollInSameThread()
                .ignoreExceptions()
                .atMost(15, TimeUnit.SECONDS)
                .with()
                .pollDelay(1, TimeUnit.SECONDS)
                .and()
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> isNewMessageReceived(email, subject, content));
    }

    private static void disableSslVerification() {
        // This is used to allow to expand a short link.
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = (hostname, session) -> true;

            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }
}

