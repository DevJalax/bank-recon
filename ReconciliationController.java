package com.example.reconciliation.controller;

import com.example.reconciliation.service.BankReconciliationService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
public class ReconciliationController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job reconciliationJob;

    @PostMapping("/api/reconciliation/upload")
    public ModelAndView uploadCSV(MultipartFile file) throws Exception {
        File csvFile = new File("input.csv");
        file.transferTo(csvFile);
        jobLauncher.run(reconciliationJob, new JobParameters());
        return new ModelAndView("redirect:/");
    }
}
