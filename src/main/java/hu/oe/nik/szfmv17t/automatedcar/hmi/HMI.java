package hu.oe.nik.szfmv17t.automatedcar.hmi;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import hu.oe.nik.szfmv17t.automatedcar.SystemComponent;
import hu.oe.nik.szfmv17t.automatedcar.bus.Signal;
import hu.oe.nik.szfmv17t.automatedcar.bus.VirtualFunctionBus;
import hu.oe.nik.szfmv17t.automatedcar.powertrainsystem.PowertrainSystem;
import hu.oe.nik.szfmv17t.automatedcar.radarsensor.RadarController;

/**
 * Created by SebestyenMiklos on 2017. 02. 26..
 */

public class HMI extends SystemComponent implements KeyListener {

    public static final char STEER_LEFT_KEY = 'a';
    public static final char STEER_RIGHT_KEY = 'd';
    public static final char INCRASE_GAS_KEY = 'w';
    public static final char DECRASE_GAS_KEY = 's';
    public static final char GEAR_UP_KEY = 'g';
    public static final char GEAR_DOWN_KEY = 'f';
    public static final char INCRASE_BRAKE_KEY = 'b';
    public static final char DECRASE_BRAKE_KEY = 'v';
    public static final char INDICATE_LEFT = 'q';
    public static final char BREAKDOWN = 'r';
    public static final char INDICATE_RIGHT = 'e';
    public static final char SEARCHING_TOGGLE = 'é';
    public static final char PARKING_TOGGLE = 'p';

    public static final int BUTTON_PRESSING_LENGTH_FOR_PTTM = 5;
    public static final int DURATION_FOR_PTTM = 100;
    public static final int CAR_SPEED_KMH_AEB_ALERT_THRESHOLD = 70;

    private int previousSteeringWheelState = 0;
    private int previousGasPedalState = 0;
    private int previousBrakePedalState = 0;
    private AutoGearStates previousGearStickState = AutoGearStates.P;
    private DirectionIndicatorStates previousDirection = DirectionIndicatorStates.Default;
    private AutomaticParkingStates previousParkingState = AutomaticParkingStates.Off;

    protected SteeringWheel steeringWheel;
    protected GasPedal gasPedal;
    protected BrakePedal brakePedal;
    protected GearStick gearStick;
    protected boolean keyPressHandled;
    protected DirectionIndicator directionIndicator;
    protected AutomaticParking parkingState;
    protected double carspeed;
    private boolean avoidableCollisionAlert;

    public void setCarspeed(double carspeed) {
        this.carspeed = carspeed * 3.6;
    }

    public HMI() {
        super();
        keyPressHandled = false;
        gasPedal = new GasPedal();
        gearStick = new GearStick();
        directionIndicator = new DirectionIndicator();
        brakePedal = new BrakePedal(directionIndicator);
        steeringWheel = new SteeringWheel();
        parkingState = new AutomaticParking();
        avoidableCollisionAlert = false;
    }

    @Override
    public void loop() {
        sendSteeringWheelSignal();
        sendGasPedalSignal();
        sendBrakePedalSignal();
        sendGearStickSignal();
        sendDirectionIndicationSignal();
        sendAutomaticParkingSignal();
        if (carspeed != 0 && steeringWheel.isSteerReleased()) {
            steeringWheel.steerRelease();
        }
    }

    private void sendSteeringWheelSignal() {
        if (steeringWheel.getState() != previousSteeringWheelState) {
            VirtualFunctionBus.sendSignal(new Signal(PowertrainSystem.SMI_SteeringWheel, steeringWheel.getState()));
            previousSteeringWheelState = steeringWheel.getState();
        }
    }

    private void sendGasPedalSignal() {
        if (gasPedal.getState() != previousGasPedalState) {
            VirtualFunctionBus.sendSignal(new Signal(PowertrainSystem.SMI_Gaspedal, gasPedal.getState()));
            previousGasPedalState = gasPedal.getState();
        }
    }

    private void sendBrakePedalSignal() {
        if (brakePedal.getState() != previousBrakePedalState) {
            VirtualFunctionBus.sendSignal(new Signal(PowertrainSystem.SMI_BrakePedal, brakePedal.getState()));
            previousBrakePedalState = brakePedal.getState();
        }
    }

    private void sendGearStickSignal() {
        if (gearStick.getAutoGearState() != previousGearStickState) {
            VirtualFunctionBus
                    .sendSignal(new Signal(PowertrainSystem.SMI_Gear, gearStick.getAutoGearState().ordinal()));
            previousGearStickState = gearStick.getAutoGearState();
        }
    }

    private void sendDirectionIndicationSignal() {
        if (directionIndicator.GetDirectionIndicatorState() != previousDirection) {
            VirtualFunctionBus
                    .sendSignal(new Signal(PowertrainSystem.SMI_Indication, directionIndicator.GetDirectionIndicatorState().ordinal()));
            previousDirection = directionIndicator.GetDirectionIndicatorState();
        }
    }

    private void sendAutomaticParkingSignal() {
        if (parkingState.getParkingState() != previousParkingState) {
            VirtualFunctionBus
                    .sendSignal(new Signal(PowertrainSystem.Parking_State, parkingState.getParkingState().ordinal()));
            previousParkingState = parkingState.getParkingState();
        }
    }

    @Override
    public void receiveSignal(Signal s) {
        // System.out.println("HMI received signal: " + s.getId() + " data: " +
        // s.getData());
        if(s.getId()== RadarController.AVOID_ALERT) {
            if((int)s.getData() > 0){
                this.avoidableCollisionAlert = true;
            }
            else{
                this.avoidableCollisionAlert = false;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
        // System.out.println("keyTyped:" + keyEvent.getKeyChar());
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        //System.out.println("keyPressed:" + keyEvent.getKeyChar());
        char key = keyEvent.getKeyChar();
        switch (key) {
            case STEER_LEFT_KEY:
                steeringWheel.steerLeft();
                parkingState.handleDriverAction();
                break;
            case STEER_RIGHT_KEY:
                steeringWheel.steerRight();
                parkingState.handleDriverAction();
                break;
            case INCRASE_GAS_KEY:
                gasPedal.setGasPedalReleased(false);
                gasPedal.acceleration();
                parkingState.handleDriverAction();
                break;
            case DECRASE_GAS_KEY:
                gasPedal.setGasPedalReleased(false);
                gasPedal.deceleration();
                parkingState.handleDriverAction();
                break;
        }
        if (keyPressHandled) {
            return;
        }
        switch (key) {
            case INCRASE_BRAKE_KEY:
                brakePedal.start();
                parkingState.handleDriverAction();
                break;
            case DECRASE_BRAKE_KEY:
                brakePedal.start();
                parkingState.handleDriverAction();
                break;
        }
        keyPressHandled = true;
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        //System.out.println("keyReleased:" + keyEvent.getKeyChar());
        keyPressHandled = false;
        char key = keyEvent.getKeyChar();
        switch (key) {
            case STEER_LEFT_KEY:
                steeringWheel.setSteerReleased(true);
                break;
            case STEER_RIGHT_KEY:
                steeringWheel.setSteerReleased(true);
                break;
            case INCRASE_GAS_KEY:
                this.addGas();

                break;
            case DECRASE_GAS_KEY:
                gasPedal.setGasPedalReleased(true);
                gasPedal.deceleration();
                break;
            case INCRASE_BRAKE_KEY:
                this.Brake();
                break;
            case DECRASE_BRAKE_KEY:
                brakePedal.releasingBrake();
                break;
            case GEAR_UP_KEY:
                gearStick.gearUpAutomatic();
                break;
            case GEAR_DOWN_KEY:
                gearStick.gearDownAutomatic();
                break;
            case INDICATE_LEFT:
                directionIndicator.IndicatingLeft();
                break;
            case INDICATE_RIGHT:
                directionIndicator.IndicatingRight();
                break;
            case BREAKDOWN:
                directionIndicator.IndicatingBreakdown();
                break;
            case SEARCHING_TOGGLE:
                parkingState.searchingButtonPress();
                break;
            case PARKING_TOGGLE:
                parkingState.parkingButtonPress();
                break;
        }
    }

    protected void addGas() {
        if(brakePedal.getState() > 0) {
            brakePedal.setState(0);
        }
        gasPedal.setGasPedalReleased(true);
        gasPedal.acceleration();
    }

    protected void Brake() {
        if(gasPedal.getState() > 0){
            gasPedal.setState(0);
        }
        brakePedal.braking();
    }

    public int getGaspedalValue() {
        return gasPedal.getState();
    }

    public int getBrakepedalValue() {
        return brakePedal.getState();
    }

    public int getSteeringWheelPosition() {
        return steeringWheel.getState();
    }

    public AutoGearStates getGearStickPosition() {
        return gearStick.getAutoGearState();
    }

    public DirectionIndicatorStates getDirectionIndicatorState() {
        return directionIndicator.GetDirectionIndicatorState();
    }

    public AutomaticParkingStates getParkingState(){return parkingState.getParkingState();}

    public double getSpeed() {
        return carspeed;
    }

    public boolean isAEBAlertIsOn() {
        return carspeed >= CAR_SPEED_KMH_AEB_ALERT_THRESHOLD;
    }

    public boolean isAvoidableCollisionAlert() {
        return avoidableCollisionAlert;
    }
}
