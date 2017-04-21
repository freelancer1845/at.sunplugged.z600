// This is a script file for explaining the currently available commands

// #### Command reference list ####

// wait( timeInSeconds )

// setPressure( pressureInMBar )

// startConveyorSimple( direction, speedInMMs ) // direction = {LEFT_TO_RIGHT, RIGHT_TO_LEFT}, speed in mm/s

// startConveyorDistance( direction, speedInMMs, distanceInCm )

// startConveyorTime( direction, speedInMMs, timeInSeconds )

// startConveyorTimeUnderCathode( direction, distanceInCm, timeUnderCathodeInSeconds )

// waitForConveyor() // the preceding commands simply start the conveyor. To wait for the conveyor to stop again
					 // you need to call this command.

// stopConveyor() // simply stops the conveyor no matter what

// setpointPowersource( PowerSourceId, setpointInKw ) // PowerSoruceId = {PINNACLE, SSV1, SSV2}, setpoint in kW
													  // a value of 0 stops the power source.
													  // if a power source stops unexpected the script is aborted!

// waitForStablePowersource( PowerSourceId ) // waits for the provied powersource to become stable.
											 // the condition is that the powersource must be in the defined window
											 // (which can be defined in the parameter.cfg) for 3 consecutive seconds

// Following is a basic example script
// The vacuum should already be provided or else the script will fail immediately

// Set the pressure to the desired pressure

setPressure(0.003)

// start the powersource pinnacle and set the setpoint to 0.4 kW

setpointPowersource(PINNACLE, 0.4)

// wait for the powersource to become stable

waitForStablePowersource(PINNACLE)

// start the conveyor to travel 10 meters with a time 30 seconds under the cathode moving from the Left side to the right

startConveyorTimeUnderCathode(LEFT_TO_RIGHT, 1000, 30)

// wait for the conveyor to stop

waitForConveyor()

// drive all the way back using the same setting

startConveyorTimeUnderCathode(RIGHT_TO_LEFT, 1000, 30)

// again wait for the conveyor

waitForConveyor()

// stop the cathode at pinnacle and start using the one at SSV2

setpointPowersource(PINNACLE, 0)

// but first change the pressure

setPressure(0.0025)

// start powersource SSV2 and set the power to 0.5kW

setpointPowersource(SSV2, 0.5)

// wait for stable powersource

waitForStablePowersource(SSV2)

// again go forward and backward again, this time 2 times.

startConveyorTimeUnderCathode(LEFT_TO_RIGHT, 1000, 20)
waitForConveyor()
startConveyorTimeUnderCathode(RIGHT_TO_LEFT, 1000, 20)
waitForConveyor()
startConveyorTimeUnderCathode(LEFT_TO_RIGHT, 1000, 20)
waitForConveyor()
startConveyorTimeUnderCathode(RIGHT_TO_LEFT, 1000, 20)
waitForConveyor()

// shut down the cathode
setpointPowersource(SSV2, 0)

// End of script. Comments are marked via leading "//"
