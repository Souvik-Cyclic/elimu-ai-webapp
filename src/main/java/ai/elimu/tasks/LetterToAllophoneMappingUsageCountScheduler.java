package ai.elimu.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import ai.elimu.dao.LetterToAllophoneMappingDao;
import ai.elimu.dao.WordDao;
import ai.elimu.model.content.Allophone;
import ai.elimu.model.content.Letter;
import ai.elimu.model.content.LetterToAllophoneMapping;
import ai.elimu.model.content.Word;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class LetterToAllophoneMappingUsageCountScheduler {
    
    private Logger logger = LogManager.getLogger();
    
    @Autowired
    private WordDao wordDao;

    @Autowired
    private LetterToAllophoneMappingDao letterSoundCorrespondenceDao;
    
    @Scheduled(cron="00 15 06 * * *") // At 06:15 every day
    public synchronized void execute() {
        logger.info("execute");
        
        logger.info("Calculating usage count for LetterToAllophoneMappings");

        // <id, usageCount>
        Map<Long, Integer> letterSoundCorrespondenceFrequencyMap = new HashMap<>();

        List<Word> words = wordDao.readAll();
        logger.info("words.size(): " + words.size());
        for (Word word : words) {
            logger.info("word.getText(): " + word.getText());
            
            for (LetterToAllophoneMapping letterSoundCorrespondence : word.getLetterSoundCorrespondences()) {
                if (!letterSoundCorrespondenceFrequencyMap.containsKey(letterSoundCorrespondence.getId())) {
                    letterSoundCorrespondenceFrequencyMap.put(letterSoundCorrespondence.getId(), word.getUsageCount());
                } else {
                    letterSoundCorrespondenceFrequencyMap.put(letterSoundCorrespondence.getId(), letterSoundCorrespondenceFrequencyMap.get(letterSoundCorrespondence.getId()) + word.getUsageCount());
                }
            }
        }

        // Update the values previously stored in the database
        for (LetterToAllophoneMapping letterSoundCorrespondence : letterSoundCorrespondenceDao.readAll()) {
            logger.info("letterSoundCorrespondence.getId(): " + letterSoundCorrespondence.getId());
            logger.info("letterSoundCorrespondence Letters: \"" + letterSoundCorrespondence.getLetters().stream().map(Letter::getText).collect(Collectors.joining()) + "\"");
            logger.info("letterSoundCorrespondence Allophones: /" + letterSoundCorrespondence.getAllophones().stream().map(Allophone::getValueIpa).collect(Collectors.joining()) + "/");
            logger.info("letterSoundCorrespondence.getUsageCount() (before update): " + letterSoundCorrespondence.getUsageCount());
            
            int newUsageCount = 0;
            if (letterSoundCorrespondenceFrequencyMap.containsKey(letterSoundCorrespondence.getId())) {
                newUsageCount = letterSoundCorrespondenceFrequencyMap.get(letterSoundCorrespondence.getId());
            }
            logger.info("newUsageCount: " + newUsageCount);
            
            letterSoundCorrespondence.setUsageCount(newUsageCount);
            letterSoundCorrespondenceDao.update(letterSoundCorrespondence);
            logger.info("letterSoundCorrespondence.getUsageCount() (after update): " + letterSoundCorrespondence.getUsageCount());
        }
        
        logger.info("execute complete");
    }
}
