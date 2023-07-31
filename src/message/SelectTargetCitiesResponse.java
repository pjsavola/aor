package message;

public class SelectTargetCitiesResponse extends Response {
    private final String[] cities;

    public SelectTargetCitiesResponse(String[] cities) {
        this.cities = cities;
    }

    public String[] getCities() {
        return cities;
    }
}
