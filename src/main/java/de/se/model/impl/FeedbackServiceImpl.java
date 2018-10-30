package de.se.model.impl;

import de.se.DB.FeedbackPersistence;
import de.se.DB.impl.FeedbackPersistenceImpl;
import de.se.data.*;
import de.se.data.enums.ServiceEnum;
import de.se.data.enums.StopProblem;
import de.se.data.enums.VehicleProblem;
import de.se.model.interfaces.FeedbackService;
import org.apache.log4j.Logger;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class FeedbackServiceImpl implements FeedbackService {
    private Logger logger = Logger.getLogger(FeedbackServiceImpl.class);

    private FeedbackPersistence feedbackPersistence;
    private List<Feedback> feedbacks;

    /**
     * This method initializes the instance variables feedback persistence and feedback list
     */
    @Override
    public void initialize() {
        this.logger.info("Initializing feedback service");
        feedbackPersistence = new FeedbackPersistenceImpl();
        this.feedbacks = feedbackPersistence.fetchFeedbacks();
    }

    @Override
    public List<Feedback> getAllFeedbacks() {
        return this.feedbacks;
    }

    @Override
    public List<Feedback> getFeedbacksInTimePeriod(Timestamp start, Timestamp end) {
        List<Feedback> feedbacksInTimePeriod = new ArrayList<>();
        for (Feedback feedback : this.feedbacks) {
            if (start.getTime() < feedback.getTimeStamp().getTime() && feedback.getTimeStamp().getTime() <= end.getTime()) {
                feedbacksInTimePeriod.add(feedback);
            }
        }
        return feedbacksInTimePeriod;
    }

    @Override
    public void saveFeedback(Feedback feedback) {
        feedbackPersistence.saveFeedback(feedback);
        if(!feedbacks.contains(feedback)){
            this.feedbacks.add(feedback);
        }

    }

    @Override
    public void removeFeedback(Feedback feedback) {
        feedbackPersistence.deleteFeedback(feedback);
        this.feedbacks.remove(feedback);
    }

    @Override
    public String getName() {
        return "FeedbackService";
    }

    @Override
    public int getFreshFeedbackID() {
        List<Feedback> feedbacks = getAllFeedbacks();

        int maxFeedbackID = 0;
        for (Feedback feedback : feedbacks) {
            if (feedback.getId() > maxFeedbackID) {
                maxFeedbackID = feedback.getId();
            }
        }

        return maxFeedbackID + 1;
    }

    @Override
    public List<Feedback> getFeedbacksForID(String id) {
        List<Feedback> idFeedbacks = new ArrayList<Feedback>();
        for(Feedback fdb : feedbacks){
            if(fdb.getPlaceInstance().equals(id)){
                idFeedbacks.add(fdb);
            }
        }

        return idFeedbacks;
    }

    @Override
    public void setFeedbackFinished(int feedbackId) throws Exception {
        for(Feedback fdb : feedbacks){
            if(fdb.getId()== feedbackId){
                if (fdb.getFinished()) {
                    this.logger.info("Feedback with ID \"" + fdb.getId() + "\" is already finished.");
                    return;
                }
                fdb.setFinished(true);
                saveFeedback(fdb);
                return;
            }
        }

        throw new Exception("Couldn't find a feedback instance for id: " + feedbackId);
    }

    @Override
    public List<Feedback> getUnfinishedFeedbacks() {
        List<Feedback> unfinishedFeedbacks = new ArrayList<>();

        for (Feedback feedback : this.feedbacks) {
            if (!feedback.getFinished()) {
                unfinishedFeedbacks.add(feedback);
            }
        }

        return unfinishedFeedbacks;
    }
}
