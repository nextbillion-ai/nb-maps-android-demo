package ai.nextbillion.dialog;

public class LogoInfo {
    LogoInfo(boolean selected,int logoId){
        this.resourceId = logoId;
        this.selected = selected;
    }
    int resourceId;
    boolean selected = false;
}
