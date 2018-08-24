package ai.gams.dronecontroller.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.Objects;

import ai.madara.knowledge.KnowledgeMap;

/**
 * Created by Amit S on 20/07/18.
 */
public class Drone {
    public String username;
    public String id;
    public long lastUpdatedTime;
    public KnowledgeMap knowledgeMap;
    public String uid;
    public LatLng latLng;
    public String prefix;

    public Drone() {

    }

    public Drone(String uid) {
        this.id = uid;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(long lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public KnowledgeMap getKnowledgeMap() {
        return knowledgeMap;
    }

    public void setKnowledgeMap(KnowledgeMap knowledgeMap) {
        this.knowledgeMap = knowledgeMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Drone drone = (Drone) o;
        return Objects.equals(id, drone.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return username;
    }
}
