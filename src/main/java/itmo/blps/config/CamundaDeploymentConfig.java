package itmo.blps.config;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Component
public class CamundaDeploymentConfig {

    private static final Logger log = LoggerFactory.getLogger(CamundaDeploymentConfig.class);

    private final RepositoryService repositoryService;
    private boolean deployed = false;

    private static final String[] BPMN_FILES = {
        "inquiry-create.bpmn",
        "inquiry-list.bpmn",
        "inquiry-resolve.bpmn",
        "inquiry-view.bpmn",
        "inquiry-visit.bpmn",
        "listing-close.bpmn",
        "listing-create.bpmn",
        "listing-delete.bpmn",
        "listing-expiry-check.bpmn",
        "listing-list.bpmn",
        "listing-promotion.bpmn",
        "listing-publish.bpmn",
        "listing-renew.bpmn",
        "listing-update.bpmn",
        "listing-view.bpmn"
    };

    private static final String[] FORM_FILES = {
        "inquiry-create-result.form",
        "inquiry-create-start.form",
        "inquiry-create.form",
        "inquiry-list-start.form",
        "inquiry-list.form",
        "inquiry-resolve-result.form",
        "inquiry-resolve-start.form",
        "inquiry-view-start.form",
        "inquiry-view.form",
        "inquiry-visit-result.form",
        "inquiry-visit-start.form",
        "listing-close-result.form",
        "listing-close-start.form",
        "listing-create-result.form",
        "listing-create.form",
        "listing-delete-result.form",
        "listing-delete-start.form",
        "listing-expiry-check-result.form",
        "listing-list-start.form",
        "listing-list.form",
        "listing-promotion-result.form",
        "listing-promotion-start.form",
        "listing-publish-result.form",
        "listing-publish-start.form",
        "listing-renew-result.form",
        "listing-renew-start.form",
        "listing-update-result.form",
        "listing-update-start.form",
        "listing-view-start.form",
        "listing-view.form"
    };

    public CamundaDeploymentConfig(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @EventListener(ContextRefreshedEvent.class)
    @Order(100)
    public synchronized void deployProcessesAndForms() {
        if (deployed) {
            return;
        }
        deployed = true;
        log.info("Starting Camunda processes and forms deployment process...");

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            
            Resource[] bpmnResources = new Resource[0];
            Resource[] formResources = new Resource[0];
            try {
                bpmnResources = resolver.getResources("classpath*:**/*.bpmn");
                formResources = resolver.getResources("classpath*:forms/*.form");
                log.info("Scanned classpath. Found {} BPMN files and {} Form files via wildcard scanning.", bpmnResources.length, formResources.length);
            } catch (Exception e) {
                log.warn("Classpath wildcard scanning failed or returned error, will fallback to explicit resource list: {}", e.getMessage());
            }

            List<Resource> bpmnList = new ArrayList<>();
            List<Resource> formList = new ArrayList<>();

            if (bpmnResources.length > 0) {
                for (Resource r : bpmnResources) {
                    bpmnList.add(r);
                }
            }
            if (formResources.length > 0) {
                for (Resource r : formResources) {
                    formList.add(r);
                }
            }

            // Fallback for BPMN
            if (bpmnList.isEmpty()) {
                log.info("BPMN list from scanning is empty. Using explicit fallback list.");
                for (String name : BPMN_FILES) {
                    ClassPathResource r = new ClassPathResource(name);
                    try (InputStream is = r.getInputStream()) {
                        if (is != null) {
                            bpmnList.add(r);
                            log.info("Successfully loaded fallback BPMN: {}", name);
                        }
                    } catch (IOException e) {
                        log.warn("Failed to load fallback BPMN: {} - {}", name, e.getMessage());
                    }
                }
            }

            // Fallback for Forms
            if (formList.isEmpty()) {
                log.info("Form list from scanning is empty. Using explicit fallback list.");
                for (String name : FORM_FILES) {
                    ClassPathResource r = new ClassPathResource("forms/" + name);
                    try (InputStream is = r.getInputStream()) {
                        if (is != null) {
                            formList.add(r);
                            log.info("Successfully loaded fallback Form: forms/{}", name);
                        }
                    } catch (IOException e) {
                        log.warn("Failed to load fallback Form: forms/{} - {}", name, e.getMessage());
                    }
                }
            }

            if (bpmnList.isEmpty()) {
                log.warn("No BPMN resources resolved. Skipping deployment.");
                return;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                for (Resource resource : bpmnList) {
                    String filename = resource.getFilename();
                    if (filename != null) {
                        log.info("Packing BPMN into ZIP for deployment: {}", filename);
                        addZipEntry(zos, filename, resource);
                    }
                }
                for (Resource resource : formList) {
                    String filename = resource.getFilename();
                    if (filename != null) {
                        log.info("Packing Form into ZIP for deployment: {}", filename);
                        addZipEntry(zos, filename, resource);
                    }
                }
            }

            byte[] zipBytes = baos.toByteArray();
            log.info("Deploying ZIP to Camunda RepositoryService. Size: {} bytes, containing {} BPMN files and {} forms.", zipBytes.length, bpmnList.size(), formList.size());
            
            Deployment deployment = repositoryService.createDeployment()
                    .name("cian-manual-deployment")
                    .enableDuplicateFiltering(true)
                    .addZipInputStream(new ZipInputStream(new ByteArrayInputStream(zipBytes)))
                    .deploy();

            long processCount = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployment.getId())
                    .count();

            log.info("Camunda deployment successful. ID: {}, Process Definitions deployed in this transaction: {}", deployment.getId(), processCount);

        } catch (Exception e) {
            log.error("Failed to deploy Camunda resources", e);
        }
    }

    private void addZipEntry(ZipOutputStream zos, String path, Resource resource) throws IOException {
        zos.putNextEntry(new ZipEntry(path));
        try (InputStream is = resource.getInputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                zos.write(buffer, 0, bytesRead);
            }
        }
        zos.closeEntry();
    }
}
