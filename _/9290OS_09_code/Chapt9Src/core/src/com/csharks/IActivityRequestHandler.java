package com.csharks;

public interface IActivityRequestHandler {
	//for google analytcis
	public void setTrackerScreenName(String path);
	//for andmod ads
	public void showAds(boolean show);
	//for google play services
	public void login();
    public void logOut();

    public boolean isSignedIn();

    public void submitScore(int score);
    public void unlockAchievement(int stars);
    
    //gets the scores/achievements and displays them threw googles default widget
    public void showScores();
    public void showAchievements();
}
