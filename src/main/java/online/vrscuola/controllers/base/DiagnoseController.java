package online.vrscuola.controllers.base;

import online.vrscuola.services.health.DatabaseHealthCheckService;
import online.vrscuola.services.health.OperatingSystemHealthCheckService;
import online.vrscuola.services.health.ResourceDirectoryHealthCheckService;
import online.vrscuola.services.health.WebsiteHealthCheckService;
import online.vrscuola.services.pdf.EventLogPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/")
public class DiagnoseController {

    @Value("${health.datasource.website}")
    private String healthDataSourceWebsite;
    @Value("${health.datasource.website.keycloak}")
    private String healthDataSourceWebsiteKeycloak;
    @Value("${health.datasource.website.risorse}")
    private String healthdataSourceWebsiteRisorse;
    @Value("${health.datasource.url}")
    private String healthDataSourceUrl;

    @Value("${health.datasource.username}")
    private String healthDatasourceUsername;
    @Value("${health.datasource.password}")
    private String healthDatasourcePassword;

    @Value("${health.datasource.website.keycloak}")
    private String linkKeycloak;

    @Value("${health.datasource.website.risorse}")
    private String linkRisorse;


    @Autowired
    DatabaseHealthCheckService databaseHealthCheckService;

    @Autowired
    ResourceDirectoryHealthCheckService resourceDirectoryHealthCheckService;

    @Autowired
    WebsiteHealthCheckService websiteHealthCheckService;

    @Autowired
    OperatingSystemHealthCheckService operatingSystemHealthCheckService;

    @Autowired
    EventLogPdfService eventLogPdfService;

    @GetMapping("/diagnosi")
    public String getDiagnose(Model model) throws IOException {
        // Recupera i valori dal JSON e assegnali al model

        String databaseStatus = databaseHealthCheckService.checkDatabase(healthDataSourceUrl, healthDatasourceUsername, healthDatasourcePassword).get("status").toString();
        String resourceDirectoryStatus =  resourceDirectoryHealthCheckService.checkResourceDirectory().get("status").toString();
        String websiteStatus =  websiteHealthCheckService.checkWebsite(healthDataSourceWebsite).get("status").toString();
        String websiteKeycloakStatus = websiteHealthCheckService.checkWebsite(healthDataSourceWebsiteKeycloak).get("status").toString();
        String websiteRisorseStatus =  websiteHealthCheckService.checkWebsite(healthdataSourceWebsiteRisorse, healthDatasourceUsername, healthDatasourcePassword).get("status").toString();
        String operatingSystemStatus = operatingSystemHealthCheckService.checkOperatingSystem().get("status").toString();

        model.addAttribute("databaseStatus", databaseStatus);
        model.addAttribute("resourceDirectoryStatus", resourceDirectoryStatus);
        model.addAttribute("websiteStatus", websiteStatus);
        model.addAttribute("websiteKeycloakStatus", websiteKeycloakStatus);
        model.addAttribute("websiteRisorseStatus", websiteRisorseStatus);
        model.addAttribute("operatingSystemStatus", operatingSystemStatus);

        model.addAttribute("utenti", linkKeycloak);
        model.addAttribute("risorse", linkRisorse);


        model.addAttribute("intestazione", "Benvenuti nel sito Vr Scuola");
        model.addAttribute("saluti", "Autenticati per utilizzare i servizi");

        eventLogPdfService.save();

        return ("diagnosi");
    }

}
