package SearchEngineApp.service.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Getter
@Setter
public class TrueResponse implements Response
{
    private final boolean result = true;

    @Override
    public boolean getResult() {
        return result;
    }
}
