package com.example.reconciliation.repository;

import com.example.reconciliation.model.BankRecord;
import com.example.reconciliation.model.InternalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BankRecordRepository extends JpaRepository<BankRecord, Long> {
    List<BankRecord> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);
}

@Repository
public interface InternalRecordRepository extends JpaRepository<InternalRecord, Long> {
    List<InternalRecord> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);
}
