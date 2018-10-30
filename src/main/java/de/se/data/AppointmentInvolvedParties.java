package de.se.data;

public class AppointmentInvolvedParties {
    public static int getPartyID(String involvedPartyName) {
        switch(involvedPartyName) {
            case "Chelsea Football Club": return 4000570;
            case "Arsenal Football Club": return 4000571;
            case "Twickenham Stoop Stadium": return 4000572;
            case "London Theatre Direct Ltd": return 4000573;
            case "Delfont Mackintosh Theatres Ltd": return 4000574;
            case "Festival Republic": return 4000575;
            case "Montefiore Centre": return 4000576;
            case "English National Ballet": return 4000577;
            case "Royal Opera House": return 4000578;
            case "People’s March for Europe": return 4000579;
            default: return -1;
        }
    }

    public static String getInvolvedPartyName(int id) {
        switch(id) {
            case 4000570: return "Chelsea Football Club";
            case 4000571: return "Arsenal Football Club";
            case 4000572: return "Twickenham Stoop Stadium";
            case 4000573: return "London Theatre Direct Ltd";
            case 4000574: return "Delfont Mackintosh Theatres Ltd";
            case 4000575: return "Festival Republic";
            case 4000576: return "Montefiore Centre";
            case 4000577: return "English National Ballet";
            case 4000578: return "Royal Opera House";
            case 4000579: return "People’s March for Europe";
            default: return "";
        }
    }
}
