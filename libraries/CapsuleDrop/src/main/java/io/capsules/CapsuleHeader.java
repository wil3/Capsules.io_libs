package io.capsules;

import java.io.Serializable;

public class CapsuleHeader implements Serializable {

    public static final long DEFAULT_CID = -1;
	/**
	 * 
	 */
	private static final long serialVersionUID = 8883149321377807274L;



    private String username;
    private int version =0;  //this is the number of times the object is updated
    private long cid = DEFAULT_CID;
	//TODO need to determine defaults
	private String name;
	private String thumbnail = "";
	//TODO change to String so we can use a UUID hex or a long
	private boolean carrying = true;
	private long totalsize;
	//private Coordinates location;
	private double lat;
	private double lon;

    //TODO neds to be changed to string or int
	private boolean moveable = true;
	private boolean visibility = true;
	//private boolean password;
	private boolean uploadable;
	public boolean inViewableRange = false;
	public boolean inPickupRange = false;
	private String hashtag = ""; //TODO this is only temperoary should be in descriptions
	private long maxCapacity;
    private String passcode = "";
    //TODO move to server dont need this client side
    private long birthdatetime = 1385874000000L;
    private long expiredatetime = 1388466000000L;

    private int clones = 0;


	public CapsuleHeader(){
		
	}

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
    public String getPasscode() {
        return passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }
    public long getCid() {
        return cid;
    }

    public void setCid(long cid) {
        this.cid = cid;
    }
    public long getBirthdatetime() {
        return birthdatetime;
    }

    public void setBirthdatetime(long birthdatetime) {
        this.birthdatetime = birthdatetime;
    }

    public long getExpiredatetime() {
        return expiredatetime;
    }

    public void setExpiredatetime(long expiredatetime) {
        this.expiredatetime = expiredatetime;
    }

    public int getClones() {
        return clones;
    }

    public void setClones(int clones) {
        this.clones = clones;
    }

    public boolean isInViewableRange() {
		return inViewableRange;
	}

	public void setInViewableRange(boolean inViewableRange) {
		this.inViewableRange = inViewableRange;
	}

	public boolean isInPickupRange() {
		return inPickupRange;
	}

	public void setInPickupRange(boolean inPickupRange) {
		this.inPickupRange = inPickupRange;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public boolean isCarrying() {
		return carrying;
	}

	public void setCarrying(boolean carrying) {
		this.carrying = carrying;
	}

	public boolean getMoveable() {
		return moveable;
	}

	public void setMoveable(boolean moveable) {
		this.moveable = moveable;
	}

	public boolean getVisibility() {
		return visibility;
	}

	public void setVisibility(boolean visibility) {
		this.visibility = visibility;
	}

    /*
	public boolean isPassword() {
		return password;
	}

	public void setPassword(boolean password) {
		this.password = password;
	}
*/
	public boolean isUploadable() {
		return uploadable;
	}

	public void setUploadable(boolean uploadable) {
		this.uploadable = uploadable;
	}

	public long getSize() {
		return totalsize;
	}

	public void setSize(long size) {
		this.totalsize = size;
	}
/*
	public Coordinates getCoordinates() {
		return location;
	}

	public void setCoordinates(Coordinates location) {
		this.location = location;
	}
	
*/	
//	public CapsuleItem getItem() {
//		return item;
//	}
//	public void setItem(CapsuleItem item) {
//		this.item = item;
//	}
	public String getFormatedSize(){
		return totalsize + "MB";
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
	public long getMaxCapacity() {
		return maxCapacity;
	}
	public void setMaxCapacity(long maxCapacity) {
		this.maxCapacity = maxCapacity;
	}
	public String getHashtag() {
		return hashtag;
	}
	public void setHashtag(String hashtag) {
		this.hashtag = hashtag;
	}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    @Override
    public String toString(){
        return "{cid=" + cid + " title=" + name + " lat=" + getLat() + " lng=" + getLon() + "max capacity= " + maxCapacity + " M=" + moveable + " V=" + visibility + "}";

    }


	
}
