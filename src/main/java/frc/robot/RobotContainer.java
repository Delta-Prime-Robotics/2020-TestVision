/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import java.util.ArrayList;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.vision.VisionThread;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.vision.TolkienPipeline;

/**
 * This class is where the bulk of the robot should be declared.  Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls).  Instead, the structure of the robot
 * (including subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems and commands are defined here...


  private final Object m_visionLock = new Object(); 
  private VisionThread m_visionThread;
  private ArrayList<MatOfPoint> m_contours;
  private CvSource m_outputStream;
  private UsbCamera m_camera1;
  //private CvSink m_cvSink;
  private Mat m_outputImg;
  private final Scalar kContourColor = new Scalar(255,0,255);//new Scalar(70,200,150);
  private final Point kTextOrg = new Point(0,160);
  private final Scalar kTextColor = new Scalar(255,255,255); //new Scalar(255,50,130);
  /**
   * The container for the robot.  Contains subsystems, OI devices, and commands.
   */
  public RobotContainer() {
    // Configure the button bindings
    configureButtonBindings();

    // new Thread(() -> {
    //   UsbCamera camera1 = CameraServer.getInstance().startAutomaticCapture("Sauron", 0);

    //   CvSink inputSink = CameraServer.getInstance().getVideo(camera1);
  
    //   Mat sourceImg = new Mat();
    //   Mat outputImg = new Mat();

    //   while (!Thread.interrupted()) {
    //     if (inputSink.grabFrame(sourceImg) == 0) {
    //       continue;
    //     }

    //     Imgproc.cvtColor(sourceImg, outputImg, Imgproc.COLOR_BGR2GRAY);
    //     outputStream.putFrame(outputImg);
    //   }  
    // }).start();

    m_camera1 = CameraServer.getInstance().startAutomaticCapture("Sauron", 0);
    //m_cvSink = CameraServer.getInstance().getVideo(m_camera1);
    m_outputStream = CameraServer.getInstance().putVideo("Gandalf", 320, 240);

    m_visionThread = new VisionThread(m_camera1, new TolkienPipeline(), pipeline -> {
      if (!pipeline.filterContoursOutput().isEmpty()) {
        synchronized (m_visionLock) {
          m_contours = pipeline.filterContoursOutput();
          m_outputImg = pipeline.resizeImageOutput();
          //Imgproc.circle(m_outputImg, new Point(100,100), 20, new Scalar(150,110,90));
          Imgproc.drawContours(m_outputImg, m_contours, -1, kContourColor);
          m_outputStream.putFrame(m_outputImg);
        }
      }
      else {
        synchronized (m_visionLock) {          
          m_outputImg = pipeline.resizeImageOutput();
          Imgproc.putText(m_outputImg, "No Target Found", kTextOrg, Core.FONT_HERSHEY_SIMPLEX, 1, kTextColor);
          m_outputStream.putFrame(m_outputImg);
        }
      }
    });
    m_visionThread.start();
  }

  /**
   * Use this method to define your button->command mappings.  Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a
   * {@link edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {
  }


  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // An ExampleCommand will run in autonomous
    return null;
  }
}
