package us.myles.ViaVersion.sounds;

public enum SoundCategory {
	

	MASTER("master", 0),
	MUSIC("music", 1),
	RECORD("record", 2),
	WEATHER("weather", 3),
	BLOCK("block", 4),
	HOSTILE("hostile", 5),
	NEUTRAL("neutral", 6),
	PLAYER("player", 7),
	AMBIENT("ambient", 8),
	VOICE("voice", 9);
	
	private String name;
	private int id;
	
	SoundCategory(String name, int id)
	{
		this.name = name;
		this.id = id;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getId()
	{
		return id;
	}

}
