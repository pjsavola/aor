package message;

import java.io.Serial;

public class SelectTargetCitiesResponse extends Response {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String[] cities;

    public SelectTargetCitiesResponse(String[] cities) {
        this.cities = cities;
    }

    public String[] getCities() {
        return cities;
    }
}
