package com.example.ReadingMails.service;


import com.example.ReadingMails.dto.MailsRequest;
import com.example.ReadingMails.entity.Person;
import com.example.ReadingMails.exceptions.AddressException;
import com.example.ReadingMails.exceptions.FileNotFoundException;
import com.example.ReadingMails.repository.PersonRepository;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MailsService {

    @Autowired
    private JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String from;

    public void sendMails(MailsRequest mailsRequest) throws MessagingException {
        //(MimeMessage is a class which extends Message class and implements Part)
        MimeMessage mimeMessage=javaMailSender.createMimeMessage();
        MimeMessageHelper helper=new MimeMessageHelper(mimeMessage,true,"UTF-8");
        helper.setFrom(from);
        helper.setSubject(mailsRequest.getSubject());
        helper.setText(mailsRequest.getMessage());
        String path= mailsRequest.getAttachment();
        try
        {
            helper.setTo(mailsRequest.getTo());
            if(!mailsRequest.getTo().endsWith(".com"))
            {
                throw new AddressException("Invalid Email format");
            }
            File file=new File(path);
            if(!file.exists())
            {
                throw new FileNotFoundException("File not found Exception");
            }
            // FileSystemResource is a class extends Abstract class
            FileSystemResource fileSystemResource=new FileSystemResource(file);
            helper.addAttachment(fileSystemResource.getFilename(),fileSystemResource);
            javaMailSender.send(mimeMessage);
        }
        catch (FileNotFoundException e) {
            throw new MessagingException("Error with file attachment: " + e.getMessage(), e);
        }
        catch (AddressException e)
        {
            throw new MessagingException(e.getMessage(),e);
        }
    }


    public void readMails() throws MessagingException {
        String host = "imap.gmail.com";  // IMAP server
        String username = "channanagoudagouda51@gmail.com";
        String password = "wafwrzwxlytpgrty";

        Properties properties = new Properties();
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", "993");
        properties.put("mail.imap.ssl.enable", "true");

        Session session=Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username,password);
            }
        });

        Store store= session.getStore("imap");
        store.connect(host,username,password);
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_ONLY);
        int totalMessages = folder.getMessageCount();
        System.out.println("total messages " + totalMessages);
        int startMessageIndex = Math.max(totalMessages - 1, 1);
        System.out.println("start index " + startMessageIndex);
        Message[] messages = folder.getMessages(startMessageIndex, totalMessages);
        ExecutorService service= Executors.newFixedThreadPool(1);
        for(Message message:messages)
        {
           service.submit(new ExecutorRunner(message));
        }
        service.shutdown();
    }

    public void readMailsOnDate(Date date) throws MessagingException {
        String host="imap.gmail.com";
        String username="channanagoudagouda51@gmail.com";
        String password="wafwrzwxlytpgrty";

        Properties properties=new Properties();
        properties.put("mail.imap.host",host);
        properties.put("mail.imap.port","993");
        properties.put("mail.imap.ssl.enable","true");

        Session session=Session.getInstance(properties,null);
        Store store=session.getStore("imap");
        store.connect(host,username,password);
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_ONLY);

        Message[] messages = folder.getMessages();
        ExecutorService service=Executors.newFixedThreadPool(10);
        for(Message message:messages)
        {
            service.submit(new DatesSort(message,date));
        }

        service.shutdown();
    }
}

class DatesSort implements Runnable
{
    private Message message;
    private Date date;
    public DatesSort(Message message, Date date)
    {
        this.message=message;
        this.date=date;
    }
    @Override
    public void run() {
        try {
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
            String format = sdf.format(message.getReceivedDate());
           SimpleDateFormat sdf1=new SimpleDateFormat("yyyy-MM-dd");
            String format1 = sdf1.format(date);
            if(format1.equals(format))
           {
               System.out.println(Arrays.toString(message.getFrom()));
               System.out.println(message.getSubject());
               System.out.println(message.getReceivedDate());
               if(message.isMimeType("multipart/*"))
               {
                   Multipart content = (Multipart) message.getContent();
                   for(int i=0;i<content.getCount();i++)
                   {
                       BodyPart bodyPart = content.getBodyPart(i);
                       System.out.println(bodyPart.getContentType());
                   }
               }
           }
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class ExecutorRunner implements Runnable
{

    private Message message;
    @Autowired
    private PersonRepository personRepository;
    public ExecutorRunner(Message message)
    {
        this.message=message;
    }
    @Override
    public void run() {
        try {
            if (message.isMimeType("multipart/*")) {
                Multipart multipart = (Multipart) message.getContent();
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart part = multipart.getBodyPart(i);

                    // Check if the part is an attachment and its content type is application/pdf
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) &&
                            part.getContentType().toLowerCase().contains("application/pdf")) {
//
                        readPdf(part);
                    }
                    if(Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())&&
                    part.getContentType().toLowerCase().contains("application/vnd.ms-excel")) {
                        readExcel(part);
                    }
                }
            }
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void readPdf(BodyPart part) throws IOException, MessagingException {
        saveAttachment(part);
        InputStream inputStream = part.getInputStream();
        PDDocument document = PDDocument.load(inputStream);
        if (document.isEncrypted()) {
            System.out.println("The PDF is encrypted, unable to extract content.");
            document.close();
            return;
        }

        // Extract text from the PDF document using PDFTextStripper
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);  // Extracts the text from the PDF
        document.close();
    }

    @Transactional
    public void readExcel(BodyPart part) throws IOException, MessagingException {
        saveAttachment(part);
        InputStream inputStream = part.getInputStream();
        Workbook workbook = new HSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);  // Get the first sheet

        // Iterate through the row
        for (Row row : sheet) {

            // Assuming data starts from row index 1 (skip header row)
            if (row.getRowNum() == 0) {
                continue;  // Skip the header row
            }

            // Extract data from cells and map to the Person entity
            for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
                Person person = new Person();
                System.out.println("loop entered");
                Cell cell = row.getCell(i);

                switch (cell.getColumnIndex()) {
                    case 0:  // id
                        if (cell.getCellType() == CellType.NUMERIC) {
                            System.out.println("ID Cell: " + cell.getNumericCellValue());
                             person.setId((int) cell.getNumericCellValue());
                        } else {
                            System.out.println("Unexpected cell type for ID");
                        }
                        break;
                    case 1:  // firstName
                        if (cell.getCellType() == CellType.STRING) {
                            System.out.println("First Name: " + cell.getStringCellValue());
                            person.setFirstName(cell.getStringCellValue());
                        } else {
                            System.out.println("Unexpected cell type for First Name");
                        }
                        break;
                    case 2:  // lastName
                        if (cell.getCellType() == CellType.STRING) {
                            System.out.println("Last Name: " + cell.getStringCellValue());
                            person.setLastName(cell.getStringCellValue());
                        } else {
                            System.out.println("Unexpected cell type for Last Name");
                        }
                        break;
                    case 3:  // gender
                        if (cell.getCellType() == CellType.STRING) {
                            System.out.println("Gender: " + cell.getStringCellValue());
                            person.setGender(cell.getStringCellValue());
                        } else {
                            System.out.println("Unexpected cell type for Gender");
                        }
                        break;
                    case 4:  // country
                        if (cell.getCellType() == CellType.STRING) {
                            System.out.println("Country: " + cell.getStringCellValue());
                            person.setCountry(cell.getStringCellValue());
                        } else {
                            System.out.println("Unexpected cell type for Country");
                        }
                        break;
                    case 5:  // age
                        if (cell.getCellType() == CellType.NUMERIC) {
                            System.out.println("Age: " + cell.getNumericCellValue());
                            person.setAge((int) cell.getNumericCellValue());
                        } else {
                            System.out.println("Unexpected cell type for Age");
                        }
                        break;
                    case 6:  // date (date field)
                        if (cell.getCellType() == CellType.STRING) {
                            String dateStr = cell.getStringCellValue();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                            System.out.println("Date: " + dateStr);
                            person.setCurrentDate(LocalDate.parse(dateStr, formatter));
                        } else {
                            System.out.println("Unexpected cell type for Date");
                        }
                        break;
                    default:
                        System.out.println("Unknown column index");
                        break;
                }
                personRepository.save(person);
            }
        }

        workbook.close();
    }



    public void saveAttachment(BodyPart part) throws MessagingException, IOException {
        String fileName = part.getFileName();
        InputStream inputStream = part.getInputStream();

        File file=new File("C:\\Users\\Sreenivas Bandaru\\Desktop\\Channanagouda\\Attachments\\"+fileName);
        try(FileOutputStream fileOutputStream=new FileOutputStream(file))
        {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            System.out.println("Attachment saved: " + file.getAbsolutePath());
        }
    }
}
