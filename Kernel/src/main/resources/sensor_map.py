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
]

if __name__ == "__main__":
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

    print("\t")

    for i in range(0, len(sensors)):
        print(f"if (index[{sensors[i][0]}] != -1) {{")
        print(
            f"    {sensors[i][4]}.{sensors[i][5]}(lhmHelper.getValue(index[{sensors[i][0]}]));"
        )
        print("}")
