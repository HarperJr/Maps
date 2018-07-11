package harper.mapsmeproject.models;

public class MapObject {
    private int id;
    private double lat;
    private double lon;
    private double rot;
    private String type;
    private double speed;
    private double lane;
    private String slope;

    public MapObject(int id, double lon, double lat) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getRot() {
        return rot;
    }

    public void setRot(double rot) {
        this.rot = rot;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getLane() {
        return lane;
    }

    public void setLane(double lane) {
        this.lane = lane;
    }

    public String getSlope() {
        return slope;
    }

    public void setSlope(String slope) {
        this.slope = slope;
    }
}
