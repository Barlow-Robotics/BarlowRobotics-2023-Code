// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants;
import frc.robot.subsystems.Arm;

public class CalibrateArmExtention extends CommandBase {
  /** Creates a new CalabrateArmExtention. */

  private Arm armSub;

  public CalibrateArmExtention(Arm a) {
    // Use addRequirements() here to declare subsystem dependencies.
    a = armSub;

    addRequirements(armSub);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    armSub.StartExtendingAtVelocty(Constants.ArmConstants.ExtentionCalibrationVelocity);
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    // armSub.StartExtendingAtVelocty(Constants.ArmConstants.ExtentionCalibrationVelocity);
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    armSub.StartExtendingAtVelocty(0);
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return armSub.IsAtMinExtension();
  }
}
