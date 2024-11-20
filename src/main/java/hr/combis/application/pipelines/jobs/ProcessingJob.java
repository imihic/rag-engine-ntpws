package hr.combis.application.pipelines.jobs;


public interface ProcessingJob {
    Long getId();
    int getStatus();
    void setStatus(int status);
}
