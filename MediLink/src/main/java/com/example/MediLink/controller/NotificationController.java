package com.example.MediLink.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.MediLink.model.PatientAccount;
import com.example.MediLink.model.Prescription;
import com.example.MediLink.model.RecordsEntity;
import com.example.MediLink.repository.PatientAccountRepository;
import com.example.MediLink.repository.PrescriptionRepository;
import com.example.MediLink.repository.RecordsRepository;
import com.example.MediLink.service.PrescriptionService;
import com.example.MediLink.service.RecordsService;

@Controller
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private RecordsService recordsService;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private RecordsRepository recordsRepository;

    @Autowired
    private PatientAccountRepository patientAccountRepository;


    @GetMapping("/returnNotification.html")
    public String returnPatient(@RequestParam("username") String username, Model model) {
        model.addAttribute("records", recordsRepository.findByStatusAndUsername("unread", username));
        model.addAttribute("username", username);
        model.addAttribute("prescription", prescriptionRepository.findByStatusAndUsername("unread", username));
        return "notification"; // Ensure this template exists
    }

    @GetMapping("/records/{id}")
    public ResponseEntity<Resource> viewRecords(@PathVariable Long id) {
        RecordsEntity fileEntity = recordsService.getFile(id).orElse(null);
        if(fileEntity.getStatus().equals("unread")){
            fileEntity.setStatus("read");
            recordsRepository.save(fileEntity);
            String username=recordsRepository.findById(id).get().getUsername();
            PatientAccount patient=patientAccountRepository.findByUsername(username).get();
            patient.setUnread(patient.getUnread()-1);
            patientAccountRepository.save(patient);
        }
        if (fileEntity == null) {
            return ResponseEntity.notFound().build();
        }
    
        ByteArrayResource resource = new ByteArrayResource(fileEntity.getFileData());
    
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileEntity.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileEntity.getFileName() + "\"")
                .contentLength(fileEntity.getFileData().length)
                .body(resource);
    }

    @GetMapping("/prescription/{id}")
    public ResponseEntity<Resource> viewPrescription(@PathVariable Long id) {
        Prescription fileEntity = prescriptionService.getFile(id).orElse(null);
        if(fileEntity.getStatus().equals("unread")){
        fileEntity.setStatus("read");
        prescriptionRepository.save(fileEntity);
        String username=prescriptionRepository.findById(id).get().getUsername();
        PatientAccount patient=patientAccountRepository.findByUsername(username).get();
        patient.setUnread(patient.getUnread()-1);
        patientAccountRepository.save(patient);
        }
        if (fileEntity == null) {
            return ResponseEntity.notFound().build();
        }
    
        ByteArrayResource resource = new ByteArrayResource(fileEntity.getFileData());
    
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileEntity.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileEntity.getFileName() + "\"")
                .contentLength(fileEntity.getFileData().length)
                .body(resource);
    }
}
