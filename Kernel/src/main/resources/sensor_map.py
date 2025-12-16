import sys

sensors = [
    (0, "CPU Total", "equals", "Load", "cpu", "setLoad"),
    (1, "CPU Package", "equals", "Temperature", "cpu", "setPackageTemperature"),
    (2, "Core Average", "equals", "Temperature", "cpu", "setAverageTemperature"),
    (3, "CPU Package", "equals", "Power", "cpu", "setPower"),
    (4, "CPU Core", "equals", "Voltage", "cpu", "setVoltage"),
    (5, "CPU Clock #1", "equals", "Clock", "cpu", "setClockBegin"),
    (
        6,
        "CPU Clock #",
        "contains",
        "Clock",
        "cpu",
        "setClockEnd",
        "Math.max(ind, index[6])",
    ),
    (34, "GPU Core", "equals", "Temperature", "gpu", "setTemperature"),
    (35, "GPU Hot Spot", "equals", "Temperature", "gpu", "setMaxTemperature"),
    (36, "GPU Package", "equals", "Power", "gpu", "setPower"),
    (37, "GPU Core", "equals", "Clock", "gpu", "setSpeed"),
    (38, "GPU Memory Total", "equals", "SmallData", "gpu", "setMemTotal"),
    (39, "GPU Memory Free", "equals", "SmallData", "gpu", "setMemFree"),
    (40, "GPU Memory Used", "equals", "SmallData", "gpu", "setMemUsed"),
    (113, "Fully-Charged Capacity", "equals", "Energy", "battery", "setCapacity"),
    (114, "Remaining Capacity", "equals", "Energy", "battery", "setRemainCapacity"),
    (115, "Voltage", "equals", "Voltage", "battery", "setVoltage"),
    (116, "Charge Current", "equals", "Current", "battery", "setCurrent"),
    (116, "Discharge Current", "equals", "Current", "battery", "setCurrent"),
    (116, "Charge/Discharge Current", "equals", "Current", "battery", "setCurrent"),
    (117, "Charge Rate", "equals", "Power", "battery", "setRate"),
    (117, "Discharge Rate", "equals", "Power", "battery", "setRate"),
    (117, "Charge/Discharge Rate", "equals", "Power", "battery", "setRate"),
    (118, "Designed Capacity", "equals", "Energy", "battery", "setDesignedCapacity"),
]

def warning_message(pos):
    return f"// THE CODE {pos} IS SCRIPT GENERATED, DON'T CHANGE THEM DIRECTLY! CHANGE THE SCRIPT {sys.argv[0]} INSTEAD"



if __name__ == "__main__":
    below_warning = warning_message("BELOW")
    above_warning = warning_message("ABOVE")
    
    print(below_warning)
    for i in range(0, len(sensors)):
        if i == 0:
            print(
                f'if (name.{sensors[i][2]}("{sensors[i][1]}") && info.equals("{sensors[i][3]}")) {{'
            )
        else:
            print(
                f'}} else if (name.{sensors[i][2]}("{sensors[i][1]}") && info.equals("{sensors[i][3]}")) {{'
            )

        if len(sensors[i]) > 6:
            assert len(sensors[i]) > 6
            print(f"    index[{sensors[i][0]}] = {sensors[i][6]};")
        else:
            print(f"    index[{sensors[i][0]}] = ind;")

    print("}")
    print(above_warning)

    print("\t")

    print(below_warning)
    for i in range(0, len(sensors)):
        #print(f"if (index[{sensors[i][0]}] != -1 && index[{sensors[i][0]}] < 254) {{")
        print(f"if (index[{sensors[i][0]}] != -1) {{")
        print(
            f"    {sensors[i][4]}.{sensors[i][5]}(lhmHelper.getValue(index[{sensors[i][0]}]));"
        )
        print("}")
        #print(f"}} else if (index[{sensors[i][0]}] >= 254) {{")
        #print(
        #    f"    {sensors[i][4]}.{sensors[i][5]}(index[{sensors[i][0]}] == 254 ? 1 : 0);\n}}"
        #)
    
    print(above_warning)
