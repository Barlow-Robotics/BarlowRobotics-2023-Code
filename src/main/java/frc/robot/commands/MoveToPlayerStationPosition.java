// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants;
import frc.robot.subsystems.Arm;
import frc.robot.subsystems.Claw;

public class MoveToPlayerStationPosition extends CommandBase {

  /***** MIGHT NEED TO MAKE ANOTHER SIMILAR COMMAND (might be another position for the ) *****/

  Arm armSub;
  Claw clawSub;

  public MoveToPlayerStationPosition(Arm a, Claw c) {
    
    armSub = a;
    clawSub = c;

    addRequirements(armSub, clawSub);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {}

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    armSub.setArmAngle(Constants.ArmConstants.playerStationArmAngle);
    armSub.setArmLength(Constants.ArmConstants.playerStationArmLength);
    clawSub.openClaw();
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {}

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}