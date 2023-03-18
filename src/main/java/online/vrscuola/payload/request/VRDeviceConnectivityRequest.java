package online.vrscuola.payload.request;

import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class VRDeviceConnectivityRequest {
    private static final Logger Log = LogManager.getLogger(VRDeviceInitRequest.class.getName());

    @NotBlank
    @Size(max = 17)
    private String macAddress;
    @NotBlank
    @Size(max = 15)
    private String label;
    @NotBlank
    @Size(max = 50)
    private String code;
    @Size(max = 255)
    private String note;



}
