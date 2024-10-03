package com.example.reconciliation.service;

import com.example.reconciliation.model.InternalRecord;
import com.example.reconciliation.repository.BankRecordRepository;
import com.example.reconciliation.repository.InternalRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class BankReconciliationService {

    @Autowired
    private BankRecordRepository bankRecordRepository;

    @Autowired
    private InternalRecordRepository internalRecordRepository;

    private static final Logger logger = LoggerFactory.getLogger(BankReconciliationService.class);

    public void processUploadedRecord(InternalRecord uploadedRecord) {
        internalRecordRepository.save(uploadedRecord);
        logger.info("Processed uploaded record: {}", uploadedRecord);
    }

    public JobExecutionListener getJobExecutionListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                logger.info("Starting reconciliation job...");
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                reconcile();
                logger.info("Reconciliation job completed.");
            }
        };
    }

    public void reconcile() {
        logger.info("Starting bank reconciliation process...");

        LocalDate today = LocalDate.now();
        List<BankRecord> bankRecords = bankRecordRepository.findByTransactionDateBetween(today.minusDays(1), today);
        List<InternalRecord> internalRecords = internalRecordRepository.findByTransactionDateBetween(today.minusDays(1), today);

        BigDecimal bankBalance = bankRecords.stream().map(BankRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal internalBalance = internalRecords.stream().map(InternalRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (!bankBalance.equals(internalBalance)) {
            logger.warn("Balances do not match! Bank Balance: {}, Internal Balance: {}", bankBalance, internalBalance);
        } else {
            logger.info("Balances match: {}", bankBalance);
        }
    }
}
