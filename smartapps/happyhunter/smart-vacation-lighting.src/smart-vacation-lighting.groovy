/**
 * Smart Vacation Lighting
 * 
 * Version  1.0 - Created. 
 * 
 * Controls light switches to simulate home occupancy.
 * This app implements the following algorithm for controlling the lights
 *      1) go through all configured lights and find the lights that are not ON
 *      2) now pick random lights up to the "Number of active lights" specified in configuration
 *      3) schedule to turn those lights at random times in the interval from now till "Max Random delay in seconds" in configuration
 *      4) go through the lights that are ON and schedule to turn them OFF in the interval from the moment last light was ON till "Max Random delay in seconds" in configuration
 *          so let say we picked one light to be turned on after 1m:25s
 *          then the app will pick the light that is already on to be turned OFF at random anywhere from 1m:25 till 1m:25+"Max Random delay in seconds"
 *      5) once the list of ligths is selected re-schedule recalculation in "frequency_minutes", so basically we will determine which lights to turn ON/OFF
 *          every "frequency_minutes"
 *      6) check every 30sec if it is time to turn ON/OFF any light
 *
 *  This algorithm will lead to the following behaviour: first you will see the number of lights turnin ON at random, then you will see lights turning OFF at random
 *  which is like you went into one room and turned ON light there first and then came back and turned light OFF in the old room
 * 
 * Source code can be found at: 
 *
 * Copyright 2016 Alex Filenkov
 * Credits: this app is based on the sources of "Vacation Light DIrector" by TIM SLAGLE
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use 
 * this file except in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */


// Automatically generated. Make future change here.
definition(
    name: "Smart Vacation Lighting",
    namespace: "HappyHunter",
    author: "Alexandr Filenkov",
    description: "test",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)


preferences {
    page name:"pageSetup"
    page name:"Setup"
    page name:"Settings"
    page name: "timeIntervalInput"

}

// Show setup page
def pageSetup() {

    def pageProperties = [
        name:       "pageSetup",
        title:      "Status",
        nextPage:   null,
        install:    true,
        uninstall:  true
    ]

    return dynamicPage(pageProperties) {
        section(""){
            paragraph "This app can be used to make your home seem occupied anytime you are away from your home. " +
                "Please use each of the the sections below to setup the different preferences to your liking. " 
        }
        section("Setup Menu") {
            href "Setup", title: "Setup", description: "", state:greyedOut()
            href "Settings", title: "Settings", description: "", state: greyedOutSettings()
        }
        section([title:"Options", mobileOnly:true]) {
            label title:"Assign a name", required:false
        }
    }
}

// Show "Setup" page
def Setup() {

    def newMode = [
        name:       "newMode",
        type:       "mode",
        title:      "Modes",
        multiple:   true,
        required:   true
    ]

    def switches = [
        name:       "switches",
        type:       "capability.switch",
        title:      "Switches",
        multiple:   true,
        required:   true
    ]
    
    def frequency_minutes = [
        name:       "frequency_minutes",
        type:       "number",
        title:      "Minutes?",
        required:   true
    ]
    
    def number_of_active_lights = [
        name:       "number_of_active_lights",
        type:       "number",
        title:      "Number of active lights",
        required:   true,
    ]

    def activation_delay = [
        name:       "activation_delay",
        type:       "number",
        title:      "Max Random delay in seconds",
        required:   true,
    ]

    def pageName = "Setup"

    def pageProperties = [
        name:       "Setup",
        title:      "Setup",
        nextPage:   "pageSetup"
    ]

    return dynamicPage(pageProperties) {

        section(""){
            paragraph "In this section you need to setup the deatils of how you want your lighting to be affected while " +
            "you are away.  All of these settings are required in order for the simulator to run correctly."
        }
        section("Turn on when motion detected:") {
            input "themotion", "capability.motionSensor", required: true, title: "Where?"
        }
        section("Simulator Triggers") {
            input newMode
            href "timeIntervalInput", title: "Times", description: timeIntervalLabel(), refreshAfterSelection:true
        }
        section("Light switches to turn on/off") {
            input switches
        }
        section("How often to cycle the lights") {
            input frequency_minutes
        }
        section("Number of active lights at any given time") {
            input number_of_active_lights
        }
        
        section("Max random delay to tun light on/off") {
            input activation_delay
        }
    }
}

// Show "Setup" page
def Settings() {

    def falseAlarmThreshold = [
        name:       "falseAlarmThreshold",
        type:       "decimal",
        title:      "Default is 2 minutes",
        required:   false
    ]

    def days = [
        name:       "days",
        type:       "enum",
        title:      "Only on certain days of the week",
        multiple:   true,
        required:   false,
        options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
    ]

    def pageName = "Settings"

    def pageProperties = [
        name:       "Settings",
        title:      "Settings",
        nextPage:   "pageSetup"
    ]
    
    def people = [
        name:       "people",
        type:       "capability.presenceSensor",
        title:      "If these people are home do not change light status",
        required:	false,
        multiple:	true
    ]

    return dynamicPage(pageProperties) {

        section(""){              
            paragraph "In this section you can restrict how your simulator runs.  For instance you can restrict on which days it will run " +
            "as well as a delay for the simulator to start after it is in the correct mode.  Delaying the simulator helps with false starts based on a incorrect mode change."
        }
        section("Delay to start simulator") {
            input falseAlarmThreshold
        }
        section("People") {
            paragraph "Not using this setting may cause some lights to remain on when you arrive home"
            input people            
        }
        section("More options") {
            input days
        } 
    }   
}

def timeIntervalInput() {
    dynamicPage(name: "timeIntervalInput") {
        section {
            input "startTimeType", "enum", title: "Starting at", options: [["time": "A specific time"], ["sunrise": "Sunrise"], ["sunset": "Sunset"]], defaultValue: "time", submitOnChange: true
            if (startTimeType in ["sunrise","sunset"]) {
                input "startTimeOffset", "number", title: "Offset in minutes (+/-)", range: "*..*", required: false
            }
            else {
                input "starting", "time", title: "Start time", required: false
            }
        }
        section {
            input "endTimeType", "enum", title: "Ending at", options: [["time": "A specific time"], ["sunrise": "Sunrise"], ["sunset": "Sunset"]], defaultValue: "time", submitOnChange: true
            if (endTimeType in ["sunrise","sunset"]) {
                input "endTimeOffset", "number", title: "Offset in minutes (+/-)", range: "*..*", required: false
            }
            else {
                input "ending", "time", title: "End time", required: false
            }
        }
    }
}


def installed() {
    initialize()
}

def updated() {
    unsubscribe();
    unschedule();
    initialize()
}

def initialize(){

    if (newMode != null) {
        subscribe(location, modeChangeHandler)
    }
    if (starting != null) {
        schedule(starting, modeChangeHandler)
    }
    subscribe(themotion, "motion.active", motionDetectedHandler)
    
    log.debug "Installed with settings: ${settings}"
}

def motionDetectedHandler(evt) {
    log.debug "motionDetectedHandler called: $evt"
    setLocationMode("Away")
    setLocationMode("Home")
}

def modeChangeHandler(evt) {
    log.debug "modeChangeHandler called: $evt"
    def delay = (falseAlarmThreshold != null && falseAlarmThreshold != "") ? falseAlarmThreshold * 60 : 2 * 60 

    Date dt = new Date();
    dt.setTime(dt.getTime() + delay*1000 - 1*1000); // when to do first recalculation for lights

    def strDate = dt.format("yyyy-MM-dd'T'HH:mm:ss.SSS zzz")
    log.debug "modeChangeHandler strDate: $strDate"

    state.scheduleAt = strDate;
    state.runCounter = 0;
    state.ligthsToOn =[]
    state.ligthsToOff=[]

    runIn(delay, scheduleCheck)
}


// control lights
def scheduleCheck(evt) {
    //log.debug "scheduleCheck:"
 
    if(allOk)
    {
        state.runCounter = state.runCounter + 1;
        /*def maxRunCount = 10
        if (state.runCounter > maxRunCount)
        {
            log.debug("Stopping after $maxRunCount iterations")
            unschedule()
            return;
        }*/

        def dt = new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSS zzz",state.scheduleAt)
        //timeToday(state.scheduleAt)

        //def zz = new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSS zzz",state.scheduleAt)
        //log.debug("Running #2: $zz")

        def now = new Date()

        log.debug("Running #$state.runCounter: Start:$dt now:$now")

        if (dt.before(now)) {
            dt = new Date();

            dt.setTime(dt.getTime() + frequency_minutes*1000*60);
            state.scheduleAt = dt.format("yyyy-MM-dd'T'HH:mm:ss.SSS zzz")
            log.debug("Recalculating and setting new time point in $frequency_minutes minutes at $state.scheduleAt")

            // grab a random switch
            def random = new Random()
            def switchesToOn = []
            def ligthsToOn = []
            def maxOnDelay = 0

            // pick only switches which are not ON
            for (int i = 0 ; i < switches.size() ; i++) {
                if ("on" != switches[i].currentSwitch) {
                    switchesToOn << i
                    log.debug("Switch not ON $i")
                }
            }

            log.debug("switchesToOn=$switchesToOn")

            // now try to turn switches that are not ON
            for (int i = 0 ; i < number_of_active_lights ; i++) {
                // if there are no inactive switches to turn on then let's break
                if (switchesToOn.size() == 0){
                    break
                }

                // grab a random switch and turn it on
                def random_idx = random.nextInt(switchesToOn.size())

                def random_sec = random.nextInt(activation_delay)

                if (maxOnDelay < random_sec){
                    maxOnDelay = random_sec
                }


                dt = new Date();
                dt.setTime(dt.getTime() + random_sec*1000);

                ligthsToOn << [switchesToOn[random_idx], dt.format("yyyy-MM-dd'T'HH:mm:ss.SSS zzz")];

                // then remove that switch from the pool off switches that can be turned on
                switchesToOn.remove(random_idx)
            }
            maxOnDelay = maxOnDelay + 1

            def strOn = ligthsToOn.toString()
            state.ligthsToOn = ligthsToOn
            log.debug("Switches to ON $ligthsToOn")


            def ligthsToOff = []
            for (int i = 0 ; i < switches.size() ; i++) {
                if ("on" == switches[i].currentSwitch) {
                    // grab a random interval to turn it off
                    def random_sec = random.nextInt(60) + maxOnDelay
                    dt = new Date();
                    dt.setTime(dt.getTime() + random_sec*1000);

                    ligthsToOff << [i, dt.format("yyyy-MM-dd'T'HH:mm:ss.SSS zzz")];
                }
            }

            def strOff = ligthsToOff.toString()
            state.ligthsToOff = ligthsToOff
            log.debug("Switches to OFF $ligthsToOff")

            dt = new Date();
            dt.setTime(dt.getTime() + 30*1000);
            runOnce(dt, executeLightsOnOff)

        } else {
            executeLightsOnOff()
            dt = new Date();
            dt.setTime(dt.getTime() + 30*1000);

            runOnce(dt, executeLightsOnOff)
        }

        // re-run again when the frequency demands it
        // schedule("seconds minutes hours day_of_month month dayofweek year", scheduleCheck)
        // 0/5 every 5 sec
        // * any
        // every minute "0 0/1 * 1/1 * ? *"
        // every 30sec "0/30 0/1 * 1/1 * ? *"
        if (state.runCounter == 1) {
            schedule("5 0/1 * 1/1 * ? *", scheduleCheck)
            log.debug "re-schedule runIn: 60 sec"
        }
    }
    //Check to see if mode is ok but not time/day.  If mode is still ok, check again after frequency period.
    else if (modeOk) {
        log.debug("mode OK. Running again")
    }
        //if none is ok turn off frequency check and turn off lights.
    else {
        if(people){
            //don't turn off lights if anyone is home
            if(someoneIsHome){
                log.debug("Stopping Check for Light")
                unschedule()
            }
            else{
                log.debug("Stopping Check for Light and turning off all lights")
                switches.off()
                unschedule()
            }
        }
        else if (!modeOk) {
            log.debug("Stopping Check for Light and unscheduling")
            unschedule()
        }
    }
}

def executeLightsOnOff() {
    def now = new Date()
/*
    Example
    now.setTimeZone(location.timeZone ?: timeZone(time))
    if (location.timeZone) {
        now.setTimeZone(location.timeZone)
    }*/

    log.debug("executeLightsOnOff #$state.runCounter: now:$now")

    // get the list of switches to turn ON
    def ligthsToOn = state.ligthsToOn;
    //log.debug("Switches to ON $ligthsToOn")
    def lightsWereTurnedOn = false

    // we need to do one iteration find the switch that has to be on (if any)
    // then once the switch is found we need to remove it from the list
    // then we can start over again, since deleting the element from the list
    // which we are iterating through is not safe
    while (true){
        // list of switches that were turned on during this iteration
        def ligthsToOnIdx = -1

        // go through all switches and check which ones are due to turn on
        ligthsToOn.eachWithIndex  { subList, idx ->
            // make sure we do iteration once and for valid entries only
            if (subList.size() == 2 && ligthsToOnIdx == -1){
                def lightIdx = subList[0]
                def lightOnTime = new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSS zzz",subList[1])

                if (lightOnTime.before(now)){
                    // turn it on
                    log.debug("Switching ON #$lightIdx")
                    switches[lightIdx].on()
                    lightsWereTurnedOn = true

                     // store index in ligthsToOn for removal
                    ligthsToOnIdx = idx
                }
            }
        }
        // if no switch was turned ON then break this loop
        if (ligthsToOnIdx == -1){
            break;
        }
        // now remove switch that we turned ON during this iteration
        ligthsToOn.remove(ligthsToOnIdx)
    }

        // update the state with a new list
    if (lightsWereTurnedOn){
        state.ligthsToOn = ligthsToOn;
    }


    // get the list of switches to turn OFF
    def ligthsToOff = state.ligthsToOff;
    //log.debug("Switches to ON $ligthsToOff")
    def lightsWereTurnedOff = false

    // we need to do one iteration find the switch that has to be off (if any)
    // then once the switch is found we need to remove it from the list
    // then we can start over again, since deleting the element from the list
    // which we are iterating through is not safe
    while (true){
        // list of switches that were turned off during this iteration
        def ligthsToOffIdx = -1

        // go through all switches and check which ones are due to turn off
        ligthsToOff.eachWithIndex  { subList, idx ->
            // make sure we do iteration once and for valid entries only
            if (subList.size() == 2 && ligthsToOffIdx == -1){
                def lightIdx = subList[0]
                def lightOnTime = new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSS zzz",subList[1])

                if (lightOnTime.before(now)){
                    // turn it off
                    log.debug("Switching OFF #$lightIdx")
                    switches[lightIdx].off()
                    lightsWereTurnedOff = true

                    // store index in ligthsToOff for removal
                    ligthsToOffIdx = idx
                }
            }
        }
        // if no switch was turned OFF then break this loop
        if (ligthsToOffIdx == -1){
            break;
        }
        // now remove switch that we turned ON during this iteration
        ligthsToOff.remove(ligthsToOffIdx)
    }

    // update the state with a new list
    if (lightsWereTurnedOff){
        state.ligthsToOff = ligthsToOff;
    }
}

//below is used to check restrictions
private getAllOk() {
    modeOk && daysOk && timeOk && homeIsEmpty
}


private getModeOk() {
    def result = !newMode || newMode.contains(location.mode)
    log.trace "modeOk = $result"
    result
}

private getDaysOk() {
    def result = true
    if (days) {
        def df = new java.text.SimpleDateFormat("EEEE")
        if (location.timeZone) {
            df.setTimeZone(location.timeZone)
        }
        else {
            df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
        }
        def day = df.format(new Date())
        result = days.contains(day)
    }
    log.trace "daysOk = $result"
    result
}

private getHomeIsEmpty() {
    def result = true

    if(people?.findAll { it?.currentPresence == "present" }) {
        result = false
    }

    log.debug("homeIsEmpty: ${result}")

    return result
}

private getSomeoneIsHome() {
    def result = false

    if(people?.findAll { it?.currentPresence == "present" }) {
        result = true
    }

    log.debug("anyoneIsHome: ${result}")

    return result
}

private getTimeOk() {
    def result = true
    def start = timeWindowStart()
    def stop = timeWindowStop()
    if (start && stop && location.timeZone) {
        result = timeOfDayIsBetween(start, stop, new Date(), location.timeZone)
    }
    log.trace "timeOk = $result"
    result
}

private timeWindowStart() {
    def result = null
    if (startTimeType == "sunrise") {
        result = location.currentState("sunriseTime")?.dateValue
        if (result && startTimeOffset) {
            result = new Date(result.time + Math.round(startTimeOffset * 60000))
        }
    }
    else if (startTimeType == "sunset") {
        result = location.currentState("sunsetTime")?.dateValue
        if (result && startTimeOffset) {
            result = new Date(result.time + Math.round(startTimeOffset * 60000))
        }
    }
    else if (starting && location.timeZone) {
        result = timeToday(starting, location.timeZone)
    }
    log.trace "timeWindowStart = ${result}"
    result
}

private timeWindowStop() {
    def result = null
    if (endTimeType == "sunrise") {
        result = location.currentState("sunriseTime")?.dateValue
        if (result && endTimeOffset) {
            result = new Date(result.time + Math.round(endTimeOffset * 60000))
        }
    }
    else if (endTimeType == "sunset") {
        result = location.currentState("sunsetTime")?.dateValue
        if (result && endTimeOffset) {
            result = new Date(result.time + Math.round(endTimeOffset * 60000))
        }
    }
    else if (ending && location.timeZone) {
        result = timeToday(ending, location.timeZone)
    }
    log.trace "timeWindowStop = ${result}"
    result
}

private hhmm(time, fmt = "h:mm a")
{
    def t = timeToday(time, location.timeZone)
    def f = new java.text.SimpleDateFormat(fmt)
    f.setTimeZone(location.timeZone ?: timeZone(time))
    f.format(t)
}

private timeIntervalLabel() {
    def start = ""
    switch (startTimeType) {
        case "time":
            if (ending) {
                start += hhmm(starting)
            }
            break
        case "sunrise":
        case "sunset":
            start += startTimeType[0].toUpperCase() + startTimeType[1..-1]
            if (startTimeOffset) {
                start += startTimeOffset > 0 ? "+${startTimeOffset} min" : "${startTimeOffset} min"
            }
            break
    }

    def finish = ""
    switch (endTimeType) {
        case "time":
            if (ending) {
                finish += hhmm(ending)
            }
            break
        case "sunrise":
        case "sunset":
            finish += endTimeType[0].toUpperCase() + endTimeType[1..-1]
            if (endTimeOffset) {
                finish += endTimeOffset > 0 ? "+${endTimeOffset} min" : "${endTimeOffset} min"
            }
            break
    }
    start && finish ? "${start} to ${finish}" : ""
}

//sets complete/not complete for the setup section on the main dynamic page
def greyedOut(){
    def result = ""
    if (switches) {
        result = "complete"
    }
    result
}

//sets complete/not complete for the settings section on the main dynamic page
def greyedOutSettings(){
    def result = ""
    if (people || days || falseAlarmThreshold ) {
        result = "complete"
    }
    result
}
