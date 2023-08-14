package message;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

public class SelectTargetCitiesResponse extends Response {
    @Serial
    private static final long serialVersionUID = 1L;
    private final List<String> cities = new ArrayList<>();

    public SelectTargetCitiesResponse() {
    }

    public SelectTargetCitiesResponse(String[] cities) {
        this.cities.addAll(List.of(cities));
    }

    public void addCity(String city) {
        cities.add(city);
    }

    public List<String> getCities() {
        return cities;
    }
}
