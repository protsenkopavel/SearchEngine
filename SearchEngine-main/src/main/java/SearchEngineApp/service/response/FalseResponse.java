package SearchEngineApp.service.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
public class FalseResponse implements Response {
    private boolean result = false;
    private String error;

    public FalseResponse(String error) {
        this.error = error;
    }

    @Override
    public boolean getResult() {
        return result;
    }
}
