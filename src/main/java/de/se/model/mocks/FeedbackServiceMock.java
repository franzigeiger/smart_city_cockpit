package de.se.model.mocks;

import de.se.data.Feedback;
import de.se.data.enums.FeedbackEnum;
import de.se.model.interfaces.FeedbackService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FeedbackServiceMock implements FeedbackService {

    private List<Feedback> feedbacks;

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
                System.out.println("Return feedback, generated at:" + feedback.getTimeStamp());
            }
        }
        return feedbacksInTimePeriod;
    }

    @Override
    public void saveFeedback(Feedback feedback) {
        feedbacks.add(feedback);
    }

    @Override
    public void removeFeedback(Feedback feedback) {
        feedbacks.remove(feedback);
    }

    @Override
    public int getFreshFeedbackID() {
        return (int )System.currentTimeMillis();
    }

    @Override
    public String getName() {
        return FeedbackServiceMock.class.toString();
    }

    @Override
    public void initialize() {
        this.feedbacks = new ArrayList<>();

        feedbacks.add(new Feedback(1, "Random content", new Date(), false,
                "Random reason", FeedbackEnum.Vehicle, "Vehicle_1000"));
        feedbacks.add(new Feedback(2, "Fancy content", new Date(), true,
                "Fancy reason", FeedbackEnum.General, "Vehicle_1001"));
        feedbacks.add(new Feedback(3, "Weird content", new Date(), false,
                "Weird reason", FeedbackEnum.Stop, "Vehicle_1002"));
    }

    @Override
    public List<Feedback> getFeedbacksForID(String id) {
        List<Feedback> list = new ArrayList<>();
        for(Feedback fdb : feedbacks){
            if(fdb.getPlaceInstance().equalsIgnoreCase(id))
            list.add(fdb);
        }
        return list;
    }


    @Override
    public void setFeedbackFinished(int feedbackId) {
        this.feedbacks.get(0).setFinished(true);
    }

    @Override
    public List<Feedback> getUnfinishedFeedbacks() {
        return new ArrayList<>();
    }
}
