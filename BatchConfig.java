package com.example.reconciliation.config;

import com.example.reconciliation.model.InternalRecord;
import com.example.reconciliation.service.BankReconciliationService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.batch.item.file.transform.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private BankReconciliationService bankReconciliationService;

    @Bean
    public Job reconciliationJob() {
        return jobBuilderFactory.get("reconciliationJob")
                .incrementer(new RunIdIncrementer())
                .start(reconciliationStep())
                .build();
    }

    @Bean
    public Step reconciliationStep() {
        return stepBuilderFactory.get("reconciliationStep")
                .<InternalRecord, InternalRecord>chunk(10)
                .reader(csvItemReader(null)) // Pass the CSV file path or resource here
                .writer(internalRecordWriter())
                .listener(bankReconciliationService.getJobExecutionListener())
                .build();
    }

    @Bean
    public FlatFileItemReader<InternalRecord> csvItemReader(String filePath) {
        FlatFileItemReader<InternalRecord> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("input.csv")); // Assuming your CSV file is in resources folder
        reader.setLineMapper(new DefaultLineMapper<InternalRecord>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames("transactionId", "amount", "transactionDate", "description");
            }});
            setFieldSetMapper(new InternalRecordFieldSetMapper());
        }});
        return reader;
    }

    @Bean
    public ItemWriter<InternalRecord> internalRecordWriter() {
        return items -> {
            for (InternalRecord record : items) {
                bankReconciliationService.processUploadedRecord(record);
            }
        };
    }

    private static class InternalRecordFieldSetMapper implements FieldSetMapper<InternalRecord> {
        @Override
        public InternalRecord mapFieldSet(FieldSet fieldSet) {
            InternalRecord record = new InternalRecord();
            record.setTransactionId(fieldSet.readString("transactionId"));
            record.setAmount(fieldSet.readBigDecimal("amount"));
            record.setTransactionDate(fieldSet.readDate("transactionDate", "yyyy-MM-dd").toLocalDate());
            record.setDescription(fieldSet.readString("description"));
            return record;
        }
    }
}
