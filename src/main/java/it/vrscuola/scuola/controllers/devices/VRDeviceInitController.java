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

package it.vrscuola.scuola.controllers.devices;

import it.vrscuola.scuola.models.InitParamModel;
import it.vrscuola.scuola.payload.response.MessageResponse;
import it.vrscuola.scuola.repositories.devices.VRDeviceInitRepository;
import it.vrscuola.scuola.services.config.ReadOculusServices;
import it.vrscuola.scuola.services.devices.VRDeviceInitServiceImpl;
import it.vrscuola.scuola.services.securities.ValidateCredentialService;
import it.vrscuola.scuola.services.utils.MessageServiceImpl;
import it.vrscuola.scuola.services.utils.UtilServiceImpl;
import it.vrscuola.scuola.utilities.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/initialize-devices")
public class VRDeviceInitController {

    @Autowired
    VRDeviceInitServiceImpl initService;

    @Autowired
    Utilities utilities;

    @SuppressWarnings("unused")
    @Autowired
    private MessageServiceImpl messageServiceImpl;

    @Autowired
    ReadOculusServices rService;

    @Autowired
    ValidateCredentialService vService;

    @Autowired
    VRDeviceInitRepository repository;

    @Autowired
    UtilServiceImpl uService;

    @PostMapping("/add")
    public ResponseEntity<?> add() {

        List<InitParamModel> macs = rService.addOculus(vService);
        String note = "aggiunta visore";
        for (InitParamModel p : macs) {
            initService.addInit(utilities, p.getMacAddress(), note, p.getCode());
        }

        return ResponseEntity.ok(new MessageResponse(messageServiceImpl.getMessage("init.add.device.activate")));
    }

    @PostMapping("/update")
    public ResponseEntity<?> update() throws Exception {

        List<InitParamModel> paramModels = rService.changeOculus(repository);

        for (InitParamModel p : paramModels) {
            boolean valid = initService.valid(p.getOldMacAddress(), p.getCode());
            if (valid) {
                try {
                    String note = "modifica visore con vecchio mac: " + p.getOldMacAddress();
                    String code = vService.generateVisorCode(p.getMacAddress());
                    initService.updateInit(utilities, p.getOldMacAddress(), p.getMacAddress(), note, code);
                    return ResponseEntity.ok(new MessageResponse(messageServiceImpl.getMessage("init.add.device.update")));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        }

        return ResponseEntity.ok(new MessageResponse(messageServiceImpl.getMessage("init.add.device.failed")));
    }

}