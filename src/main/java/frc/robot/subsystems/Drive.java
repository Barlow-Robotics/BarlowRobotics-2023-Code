// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.TalonFXFeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.DifferentialDriveOdometry;
import edu.wpi.first.math.kinematics.DifferentialDriveWheelSpeeds;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.sim.PhysicsSim;

/** Represents a differential drive style drivetrain. */
public class Drive extends SubsystemBase {
    WPI_TalonFX driveMotorLeftLeader;
    WPI_TalonFX driveMotorLeftFollower;
    WPI_TalonFX driveMotorRightLeader;
    WPI_TalonFX driveMotorRightFollower;

    DifferentialDrive diffDrive;

    public final ADXRS450_Gyro gyro = new ADXRS450_Gyro();
    // public final AHRS navX = new AHRS();

    // private final DifferentialDriveKinematics kinematics = new
    // DifferentialDriveKinematics(
    // Constants.DriveConstants.trackWidth);
    public final DifferentialDriveOdometry odometry;

    boolean simulationInitialized = false;

    // Gains are for example purposes only - must be determined for your own robot!
    // private final SimpleMotorFeedforward m_feedforward = new
    // SimpleMotorFeedforward(1, 3);

    /**
     * Constructs a differential drive object. Sets the encoder distance per pulse
     * and resets the
     * gyro.
     */
    public Drive() {

        driveMotorLeftLeader = new WPI_TalonFX(Constants.DriveConstants.DriveMotorLeftLeaderID);
        driveMotorLeftFollower = new WPI_TalonFX(Constants.DriveConstants.DriveMotorLeftFollowerID);
        driveMotorRightLeader = new WPI_TalonFX(Constants.DriveConstants.DriveMotorRightLeaderID);
        driveMotorRightFollower = new WPI_TalonFX(Constants.DriveConstants.DriveMotorRightFollowerID);

        // Config Motors
        driveMotorLeftLeader.configFactoryDefault();
        driveMotorLeftFollower.configFactoryDefault();

        driveMotorRightLeader.configFactoryDefault();
        driveMotorRightFollower.configFactoryDefault();

        setMotorConfig(driveMotorLeftLeader);
        setMotorConfig(driveMotorRightLeader);
        setMotorConfig(driveMotorLeftFollower);
        setMotorConfig(driveMotorRightFollower);

        driveMotorLeftFollower.follow(driveMotorLeftLeader);
        driveMotorRightFollower.follow(driveMotorRightLeader);
        driveMotorRightLeader.setInverted(InvertType.InvertMotorOutput);
        driveMotorRightFollower.setInverted(InvertType.FollowMaster);
        driveMotorLeftFollower.setInverted(InvertType.FollowMaster);

        driveMotorLeftLeader.setSensorPhase(true);
        driveMotorRightLeader.setSensorPhase(true);

        diffDrive = new DifferentialDrive(driveMotorLeftLeader, driveMotorRightLeader);
        diffDrive.setSafetyEnabled(false);

        odometry = new DifferentialDriveOdometry(gyro.getRotation2d(), getLeftDistance(), getRightDistance());

        setDefaultNeutralMode();
        gyro.reset();
        CreateNetworkTableEntries();
    }

    public void periodic() {
        // Update the odometry in the periodic block
        odometry.update(
                gyro.getRotation2d(),
                getLeftDistance(),
                getRightDistance());

        NetworkTableInstance.getDefault().getEntry("drive/left_motor_distance").setDouble(getLeftDistance());
        NetworkTableInstance.getDefault().getEntry("drive/right_motor_distance").setDouble(getRightDistance());
        NetworkTableInstance.getDefault().getEntry("drive/left_encoder_count")
                .setDouble(driveMotorLeftLeader.getSelectedSensorPosition());
        NetworkTableInstance.getDefault().getEntry("drive/right_encoder_count")
                .setDouble(driveMotorRightLeader.getSelectedSensorPosition());
        // NetworkTableInstance.getDefault().getEntry("drive/leftSpeed").setDouble(getLeftSpeed());
        // NetworkTableInstance.getDefault().getEntry("drive/rightSpeed").setDouble(getRightSpeed());
        // NetworkTableInstance.getDefault().getEntry("drive/gyro_heading").setDouble(getGyroHeading());
        NetworkTableInstance.getDefault().getEntry("drive/odometry/X").setDouble(odometry.getPoseMeters().getX());
        NetworkTableInstance.getDefault().getEntry("drive/odometry/Y").setDouble(odometry.getPoseMeters().getY());
        // NetworkTableInstance.getDefault().getEntry("drive/odometry/theta")
        // .setDouble(m_odometry.getPoseMeters().getRotation().getDegrees());
    }

    public void setDefaultNeutralMode() {
        driveMotorLeftLeader.setNeutralMode(NeutralMode.Brake);
        driveMotorRightLeader.setNeutralMode(NeutralMode.Brake);
        // driveMotorLeftLeader.setNeutralMode(NeutralMode.Coast);
        // driveMotorRightLeader.setNeutralMode(NeutralMode.Coast);
    }

    /**
     * Sets the desired wheel speeds.
     *
     * @param speeds The desired wheel speeds.
     */
    public void setSpeeds(DifferentialDriveWheelSpeeds speeds) {
        driveMotorLeftLeader.set(TalonFXControlMode.Velocity,
                (speeds.leftMetersPerSecond * Constants.DriveConstants.MetersPerSecondToCountsPerSecond));
        driveMotorRightLeader.set(TalonFXControlMode.Velocity,
                (speeds.rightMetersPerSecond * Constants.DriveConstants.MetersPerSecondToCountsPerSecond));
        NetworkTableInstance.getDefault().getEntry("drive/left_speed")
                .setDouble(speeds.leftMetersPerSecond * Constants.DriveConstants.MaxSpeed);
        NetworkTableInstance.getDefault().getEntry("drive/right_speed")
                .setDouble(speeds.rightMetersPerSecond * Constants.DriveConstants.MaxSpeed);
    }

    /**
     * Sets the desired wheel speeds.
     *
     * @param leftSpeed  The desired wheel speed in meters/second
     * @param rightSpeed The desired wheel speed in meters/second
     */
    public void setSpeeds(double leftSpeed, double rightSpeed) {
        driveMotorLeftLeader.set(TalonFXControlMode.Velocity,
                (leftSpeed * Constants.DriveConstants.MetersPerSecondToCountsPerSecond / 10.0));
        driveMotorRightLeader.set(TalonFXControlMode.Velocity,
                (rightSpeed * Constants.DriveConstants.MetersPerSecondToCountsPerSecond / 10.0));

        NetworkTableInstance.getDefault().getEntry("drive/left_speed")
                .setDouble(leftSpeed * Constants.DriveConstants.MaxSpeed);
        NetworkTableInstance.getDefault().getEntry("drive/right_speed")
                .setDouble(rightSpeed * Constants.DriveConstants.MaxSpeed);
    }

    // private double getLeftSpeed() {
    // double s = driveMotorLeftLeader.getSelectedSensorVelocity() * 10.0
    // * (1.0 / Constants.DriveConstants.MetersPerSecondToCountsPerSecond);
    // return (s);
    // }

    // private double getRightSpeed() {
    // double s = driveMotorRightLeader.getSelectedSensorVelocity() * 10.0
    // * (1.0 / Constants.DriveConstants.MetersPerSecondToCountsPerSecond);
    // return (s);
    // }

    private double getLeftDistance() {
        double d = (driveMotorLeftLeader.getSelectedSensorPosition()
                / Constants.DriveConstants.CountsPerWheelRevolution)
                * Constants.DriveConstants.MetersPerRevolution;
        return (d);
    }

    private double getRightDistance() {
        double d = (driveMotorRightLeader.getSelectedSensorPosition()
                / Constants.DriveConstants.CountsPerWheelRevolution)
                * Constants.DriveConstants.MetersPerRevolution;
        return (d);
    }

    /**
     * Drives the robot with the given linear velocity and angular velocity.
     *
     * @param xSpeed       Linear velocity in m/s.
     * @param rot          Angular velocity in rad/s.
     * @param squareInputs Decreases input sensitivity at low speeds.
     */
    @SuppressWarnings("ParameterName")
    public void drive(double xSpeed, double rot, boolean squareInputs) {
        if ( xSpeed < -0.2) {
            int wpk = 1 ;
        }
        DifferentialDrive.WheelSpeeds speeds = DifferentialDrive.arcadeDriveIK(xSpeed, rot, squareInputs);
        setSpeeds(speeds.left * Constants.DriveConstants.MaxSpeed, speeds.right * Constants.DriveConstants.MaxSpeed);
        // *** need to reduce max speed when arm is extended??

        NetworkTableInstance.getDefault().getEntry("drive/xSpeed").setDouble(xSpeed);
        NetworkTableInstance.getDefault().getEntry("drive/rot").setDouble(rot);
        NetworkTableInstance.getDefault().getEntry("drive/ik_left_speed").setDouble(speeds.left);
        NetworkTableInstance.getDefault().getEntry("drive/ik_right_speed").setDouble(speeds.right);
    }

    public Pose2d getPose() {
        return odometry.getPoseMeters();
    }

    public void resetOdometry(Pose2d pose) {
        odometry.resetPosition(gyro.getRotation2d(), getLeftDistance(), getRightDistance(), pose);
        // odometry.resetPosition(gyro.getRotation2d(), 0.0, 0.0, pose);
    }

    public void resetEncoders() {
        driveMotorLeftLeader.setSelectedSensorPosition(0);
        driveMotorRightLeader.setSelectedSensorPosition(0);
    }

    public void setMaxOutput(double maxOutput) {
        diffDrive.setMaxOutput(maxOutput);
    }

    public double getGyroHeading() {
        return Math.IEEEremainder(gyro.getAngle(), 360);
    }

    public Rotation2d getHeading() {
        return Rotation2d.fromDegrees(getGyroHeading());
    }

    public void resetHeading() {
        gyro.reset();
    }

    private void CreateNetworkTableEntries() {
        NetworkTableInstance.getDefault().getEntry("drive/left_motor_distance").setDouble(0.0);
        NetworkTableInstance.getDefault().getEntry("drive/right_motor_distance").setDouble(0.0);
        NetworkTableInstance.getDefault().getEntry("drive/rotation").setDouble(0.0);

        NetworkTableInstance.getDefault().getEntry("drive/leftSpeed").setDouble(0.0);
        NetworkTableInstance.getDefault().getEntry("drive/rightSpeed").setDouble(0.0);

        NetworkTableInstance.getDefault().getEntry("drive/xSpeed").setDouble(0.0);
        NetworkTableInstance.getDefault().getEntry("drive/rot").setDouble(0.0);
        NetworkTableInstance.getDefault().getEntry("drive/arcadeDrive").setDouble(0.0);

        NetworkTableInstance.getDefault().getEntry("drive/leftVolts").setDouble(0.0);
        NetworkTableInstance.getDefault().getEntry("drive/rightVolts").setDouble(0.0);

        // NetworkTableInstance.getDefault().getEntry("drive/pose/x").setDouble(0.0);
        // NetworkTableInstance.getDefault().getEntry("drive/pose/y").setDouble(0.0);
        // NetworkTableInstance.getDefault().getEntry("drive/pose/rotation").setDouble(0.0);
    }

    private void setMotorConfig(WPI_TalonFX motor) { // changed to TalonFX for intake
        motor.configClosedloopRamp(Constants.DriveConstants.ClosedVoltageRampingConstant);
        motor.configOpenloopRamp(Constants.DriveConstants.ManualVoltageRampingConstant);
        motor.config_kF(Constants.DriveConstants.PID_id, Constants.DriveConstants.kF);
        motor.config_kP(Constants.DriveConstants.PID_id, Constants.DriveConstants.kP);
        motor.config_kI(Constants.DriveConstants.PID_id, Constants.DriveConstants.kI);
        motor.config_kD(Constants.DriveConstants.PID_id, Constants.DriveConstants.kD);

        /* Config sensor used for Primary PID [Velocity] */
        motor.configSelectedFeedbackSensor(TalonFXFeedbackDevice.IntegratedSensor, 0, 30);
    }

    public void simulationInit() {
        // PhysicsSim.getInstance().addTalonFX(driveMotorLeftLeader, 0.75, 6800, false);
        // PhysicsSim.getInstance().addTalonFX(driveMotorLeftFollower, 0.75, 6800,
        // false);
        // PhysicsSim.getInstance().addTalonFX(driveMotorRightLeader, 0.75, 6800,
        // false);
        // PhysicsSim.getInstance().addTalonFX(driveMotorRightFollower, 0.75, 6800,
        // false);
    }

    @Override
    public void simulationPeriodic() {
        if (!simulationInitialized) {
            simulationInit();
            simulationInitialized = true;
        }
        PhysicsSim.getInstance().run();

        // double headingNoise = 0.0; // (Math.random() - 0.5) * 4.0 ;
        // gyroSim.setAngle(this.m_odometry.getPoseMeters().getRotation().getDegrees() +
        // headingNoise);
        // gyroSim.setAngle(5.0);
        // gyroSim.setRate(1.0);
        NetworkTableInstance.getDefault().getEntry("drive/gyro/getAngle").setDouble(gyro.getAngle());
    }
}
