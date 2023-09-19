/**
 * Copyright (c) 2023, Benedetto Pellerito
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.vrscuola.scuola.controllers.base;

import it.vrscuola.scuola.services.ArgomentiDirService;
import it.vrscuola.scuola.services.StudentService;
import it.vrscuola.scuola.services.devices.VRDeviceManageDetailService;
import it.vrscuola.scuola.services.devices.VRDeviceManageService;
import it.vrscuola.scuola.services.devices.VRDeviceManagerOrderService;
import it.vrscuola.scuola.services.securities.KeycloakUserService;
import it.vrscuola.scuola.utilities.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Arrays;

@Controller
@RequestMapping("/sezione")
public class SezioneController {

    @Autowired
    KeycloakUserService keycloakUserService;
    @Autowired
    private ArgomentiDirService argomentiDirService;

    @Autowired
    VRDeviceManagerOrderService orderService;

    @Autowired
    VRDeviceManageDetailService detailService;

    @Autowired
    StudentService studentService;

    @Autowired
    VRDeviceManageService manageService;


    @PostMapping
    public String handleClasseSelection(@RequestParam("classSelected") String classSelected,@RequestParam("sectionSelected") String sectionSelected, HttpSession session) {
        try {
            session.setAttribute("classSelected", classSelected);
            session.setAttribute("sectionSelected", sectionSelected);

            keycloakUserService.initFilterSections(classSelected,sectionSelected);
            String[] alunni = keycloakUserService.filterSectionsAllievi();
            String[] username = keycloakUserService.filterSectionsUsername();

            String classroom = session != null && session.getAttribute("classroomSelected") != null ?  session.getAttribute("classroomSelected").toString() : "";

            session.setAttribute("usernameSelected",username);
            // cancella i dati precedenti se cambia classe in connec sovrascrivi i dati precedenti
            if(username != null && username.length > 0){
                int row = studentService.deleteUserNotClass(username[0]);
                if (row > 0) {
                    String[] alu = (String[]) session.getAttribute("alunni");
                    String[] vis = manageService.allDevices(classroom);
                    studentService.cleanVisori(session);
                    if (alu != null && alu.length > 0 && vis != null && vis.length > 0 ){
                        studentService.init(Arrays.asList(alu), Arrays.asList(vis), classroom);
                    }
                }
            }

            orderService.initOrder(alunni,username,detailService);
            String[] alunniOrdinati = orderService.getAlunni();
            String[] usernameOrdinati = orderService.getUsername();
            session.setAttribute("alunni",alunniOrdinati);
            session.setAttribute("username",usernameOrdinati);
            session.setAttribute("argoments", argomentiDirService.getArgomentiAll(Constants.PREFIX_CLASSROOM + classroom ,classSelected,sectionSelected));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/abilita-visore";
    }

}