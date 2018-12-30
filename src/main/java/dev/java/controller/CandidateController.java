package dev.java.controller;

import dev.java.db.daos.CandidateDao;
import dev.java.db.model.Attachment;
import dev.java.db.model.Candidate;
import dev.java.db.model.ContactDetails;
import dev.java.db.model.Vacancy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class CandidateController extends AbstractController<Candidate> {
    @Autowired
    private CandidateDao candidateDao;

    @PostConstruct
    @Override
    public void initialize() {
        super.initialize();
        setSortedField("surname");
        setUrl("/candidate/");

        setAbstractDao(candidateDao);
    }

    @Override
    @GetMapping("/candidates")
    public ResponseEntity getAllEntities(HttpServletRequest request) {
        return super.getAllEntities(request);
    }

    @Override
    @PostMapping("/candidates")
    public ResponseEntity createEntity(@RequestBody Candidate candidate, HttpServletRequest request) {
        return super.createEntity(candidate, request);
    }

    @Override
    @GetMapping("/candidate/{id:\\d+}")
    public ResponseEntity getEntity(@PathVariable long id, HttpServletRequest request) {
        return super.getEntity(id, request);
    }

    @Override
    @PutMapping("/candidate/{id:\\d+}")
    public ResponseEntity updateEntity(@PathVariable long id, @RequestBody Candidate candidate,
                                       HttpServletRequest request) {
        return super.updateEntity(id, candidate, request);
    }

    @Override
    @DeleteMapping("/candidate/{id:\\d+}")
    public ResponseEntity deleteEntity(@PathVariable long id, HttpServletRequest request) {
        return super.deleteEntity(id, request);
    }


    @GetMapping("/candidates/search/personal_info")
    public ResponseEntity getCandidatesByPersonalInfo(@RequestBody Candidate candidate, HttpServletRequest request) {
        getLogging().runMe(request);
        List<Candidate> candidates;
        try {
            candidates = candidateDao.getCandidatesByPersonalData(candidate);
            return ResponseEntity.ok(candidates);
        } catch (Exception e) {
            return getResponseEntityOnServerError(e);
        }
    }

    @GetMapping("/candidates/search/contacts")
    public ResponseEntity getCandidatesByContacts(@RequestBody ContactDetails contact, HttpServletRequest request) {
        getLogging().runMe(request);
        List<Candidate> candidates;
        try {
            candidates = candidateDao.getCandidatesByContact(contact);
            return ResponseEntity.ok(candidates);
        } catch (Exception e) {
            return getResponseEntityOnServerError(e);
        }
    }

//    @GetMapping("/candidates/search/skills")
//    public ResponseEntity getCandidatesBySkills(@RequestBody List<Skill> skills, HttpServletRequest request){
//        getLogging().runMe(request);
//        List<Candidate> candidates;
//        try {
//            CandidateDao candidateDao = new CandidateDao(getSessionFactory());
//            candidates = candidateDao.getCandidatesBySkills(skills);
//            return ResponseEntity.ok(candidates);
//        } catch (Exception e) {
//            return getResponseEntityOnServerError(e);
//        }
//    }


    @GetMapping("/candidate/{id:\\d+}/vacancies")
    public ResponseEntity getCorrespondVacancies(@PathVariable long id, HttpServletRequest request) {
        getLogging().runMe(request);
        try {
            Candidate entity = getAbstractDao().getEntityById(id);
            if (entity == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(entity.getVacancies());
        } catch (Exception e) {
            getLogging().runMe(e);
            return getResponseEntityOnServerError(e);
        }
    }

    @PutMapping("/candidate/{id:\\d+}/vacancies")
    public ResponseEntity updateCorrespondVacancies(@PathVariable long id, @RequestBody List<Vacancy> vacancies,
                                                 HttpServletRequest request) {
        getLogging().runMe(request);
        try {
            Candidate entity = getAbstractDao().getEntityById(id);
            if (entity == null) {
                return ResponseEntity.notFound().build();
            }
            entity.setVacancies(vacancies);
            getAbstractDao().updateEntity(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return getResponseEntityOnServerError(e);
        }
    }

    @PostMapping(value = "/candidate/{id:\\d+}/uploadAttachment", consumes = "multipart/form-data")
    public ResponseEntity uploadAttachment(@PathVariable long id, @RequestParam("file") MultipartFile file,
                                           @RequestParam("type") String type, HttpServletRequest request) {
        /*if (file.isEmpty()) {
            return new ResponseEntity<>("please select a file!", HttpStatus.OK);
        }*/
        getLogging().runMe(request);
        try {
            String filePath = saveUploadedFiles(Arrays.asList(file));
            Candidate candidate = getAbstractDao().getEntityById(id);
            if (candidate == null) {
                return ResponseEntity.notFound().build();
            }
            Attachment attachment = new Attachment();
            attachment.setAttachmentType(Attachment.AttachmentType.valueOf(type));
            attachment.setFilePath(filePath);
            candidate.getAttachments().add(attachment);
            getAbstractDao().updateEntity(candidate);
            return ResponseEntity.created(new URI(getUrl() + candidate.getId())).build();
        } catch (Exception e) {
            getLogging().runMe(e);
            return getResponseEntityOnServerError(e);
        }
    }

    private String saveUploadedFiles(List<MultipartFile> files) throws IOException {
        Path path = null;
        for (MultipartFile file : files) {
            byte[] bytes = file.getBytes();
            path = Paths.get(GeneralConstant.UPLOADED_FOLDER + file.getOriginalFilename());
            Files.write(path, bytes);
        }
        return path.toAbsolutePath().toString();
    }



    @GetMapping(value = "/candidate/{id:\\d+}/timeline")
    public ResponseEntity getTimeline(@PathVariable long id, HttpServletRequest request) {
        getLogging().runMe(request);

        try {
            Candidate entity = getAbstractDao().getEntityById(id);
            if (entity == null) {
                System.out.println("here");
                return ResponseEntity.notFound().build();
            }
            List<TimeLine> timeLines = entity.getInterviews().stream()
                    .map(interview -> new TimeLine(interview.getPlanDate(), interview.getFactDate(), interview))
                    .collect(Collectors.toList());

            timeLines.addAll(
                    entity.getExperiences().stream()
                    .map(experience -> new TimeLine(new Timestamp(experience.getDateFrom().getTime()),
                            new Timestamp(experience.getDateTo().getTime()), experience))
                    .collect(Collectors.toList())
            );
            timeLines.sort((o1, o2) -> {
                long dateDifference = o1.date1.getTime() - o2.date1.getTime();
                if (dateDifference > 0) {
                    return 1;
                }
                if (dateDifference < 0) {
                    return -1;
                }
                dateDifference = o1.date2.getTime() - o2.date2.getTime();
                if (dateDifference > 0) {
                    return 1;
                }
                if (dateDifference < 0) {
                    return -1;
                }
                return 0;
            });
            List<Object> interviewsAndExperiences = timeLines.stream()
                    .map(timeLine -> timeLine.correspondObject)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(interviewsAndExperiences);
        } catch (Exception e) {
            getLogging().runMe(e);
            return getResponseEntityOnServerError(e);
        }

    }

    private static class TimeLine {
        private Timestamp date1;
        private Timestamp date2;
        private Object correspondObject;

        TimeLine(Timestamp date1, Timestamp date2, Object correspondObject) {
            this.date1 = date1;
            this.date2 = date2;
            this.correspondObject = correspondObject;
        }
    }
}
