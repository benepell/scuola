package online.vrscuola.controllers.devices;


import online.vrscuola.payload.request.VRDeviceConnectivityConnectRequest;
import online.vrscuola.payload.request.VRDeviceConnectivityRequest;
import online.vrscuola.payload.response.MessageResponse;
import online.vrscuola.services.devices.VRDeviceConnectivityServiceImpl;
import online.vrscuola.services.devices.VRDeviceInitServiceImpl;
import online.vrscuola.utilities.MessageServiceImpl;
import online.vrscuola.utilities.UtilServiceImpl;
import online.vrscuola.utilities.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/connectivity-devices")
public class VRDeviceConnectivityController {

    @Value("${keycloak.credentials.secret}")
    private String code;

    @Autowired
    VRDeviceConnectivityServiceImpl cService;


    @Autowired
    VRDeviceInitServiceImpl iService;

    @Autowired
    Utilities utilities;

    @SuppressWarnings("unused")
    @Autowired
    private MessageServiceImpl messageServiceImpl;

    @Autowired
    UtilServiceImpl uService;

    @PostMapping(value = "/username")
    public ResponseEntity<?> username(@Valid VRDeviceConnectivityRequest request) {

        if(!cService.valid(request.getMacAddress(),request.getCode())){
            return uService.responseMsgKo(ResponseEntity.badRequest(),messageServiceImpl.getMessage("init.add.error.macaddress"));
        }

        String username = cService.viewConnect(utilities,request.getMacAddress(),request.getNote());

        return ResponseEntity.ok(new MessageResponse(username));

    }

    @PostMapping(value = "/connect")
    public ResponseEntity<?> connect(@Valid VRDeviceConnectivityConnectRequest request) {

        String username = request.getUsername();
        String macAddress = request.getMacAddress();
        String note = request.getNote();

        // dispositivo registrato
        if(!iService.valid(macAddress,code)){
            return uService.responseMsgKo(ResponseEntity.badRequest(),messageServiceImpl.getMessage("init.add.device.not.connect"));
        }

        String usernameexist = cService.viewConnect(utilities,request.getMacAddress(),request.getNote());
        if(usernameexist != null &&usernameexist.equals(username)){
            cService.connect(true ,utilities,macAddress,username,note);
        } else {
            cService.connect(false ,utilities,macAddress,username,note);
        }

        // ritorna label visore
        String visore = iService.label(macAddress);

        return ResponseEntity.ok(new MessageResponse(messageServiceImpl.getMessage("init.add.device.connect") + " [" + visore + "]"));
    }

}
