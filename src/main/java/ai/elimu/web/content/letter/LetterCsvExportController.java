package ai.elimu.web.content.letter;

import ai.elimu.dao.LetterDao;
import ai.elimu.model.content.Allophone;
import ai.elimu.model.content.Letter;
import ai.elimu.model.enums.Language;
import ai.elimu.util.ConfigHelper;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/content/letter/list")
public class LetterCsvExportController {
    
    private final Logger logger = Logger.getLogger(getClass());
    
    @Autowired
    private LetterDao letterDao;
    
    @RequestMapping(value="/letters.csv", method = RequestMethod.GET)
    public void handleRequest(
            HttpServletResponse response,
            OutputStream outputStream) {
        logger.info("handleRequest");
        
        // Generate CSV file
        String csvFileContent = "id,text,allophone_values_ipa,allophone_ids,usage_count" + "\n";
        Language language = Language.valueOf(ConfigHelper.getProperty("content.language"));
        List<Letter> letters = letterDao.readAllOrdered(language);
        logger.info("letters.size(): " + letters.size());
        for (Letter letter : letters) {
            logger.info("letter.getText(): \"" + letter.getText() + "\"");
            
            String[] allophoneValuesIpaArray = new String[letter.getAllophones().size()];
            int index = 0;
            for (Allophone allophone : letter.getAllophones()) {
                allophoneValuesIpaArray[index] = allophone.getValueIpa();
                index++;
            }
            
            long[] allophoneIdsArray = new long[letter.getAllophones().size()];
            index = 0;
            for (Allophone allophone : letter.getAllophones()) {
                allophoneIdsArray[index] = allophone.getId();
                index++;
            }
            
            csvFileContent += letter.getId() + ","
                    + "\"" + letter.getText() + "\","
                    + Arrays.toString(allophoneValuesIpaArray) + ","
                    + Arrays.toString(allophoneIdsArray) + ","
                    + letter.getUsageCount() + "\n";
        }
        
        response.setContentType("text/csv");
        byte[] bytes = csvFileContent.getBytes();
        response.setContentLength(bytes.length);
        try {
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            logger.error(null, ex);
        }
    }
}
