package hr.lknezevic.brassbirmingham.model.game;

import java.io.Serializable;
import java.util.Objects;

public final class BoardEdge implements Serializable {

    private static final long serialVersionUID = 1L;

    private final CityId cityA;
    private final CityId cityB;

    public BoardEdge(CityId cityA, CityId cityB) {
        if (cityA.compareTo(cityB) <= 0) {
            this.cityA = cityA;
            this.cityB = cityB;
        } else {
            this.cityA = cityB;
            this.cityB = cityA;
        }
    }

    public CityId getCityA() {
        return cityA;
    }

    public CityId getCityB() {
        return cityB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoardEdge that)) return false;
        return cityA == that.cityA && cityB == that.cityB;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cityA, cityB);
    }

    @Override
    public String toString() {
        return cityA + " -- " + cityB;
    }
}
