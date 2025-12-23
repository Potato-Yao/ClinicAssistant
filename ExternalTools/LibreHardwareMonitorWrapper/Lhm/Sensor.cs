using LibreHardwareMonitor.Hardware;

namespace LibreHardwareMonitorWrapper.Lhm;

public class Sensor : BaseHardware
{
    private readonly ISensor _mSensor;

    public Sensor(string id, string name, string info, ISensor sensor, int index) : base(id, name,
        info, index, HardwareType.Sensor)
    {
        _mSensor = sensor;
    }

    public int Value()
    {
        return _mSensor.Value.HasValue ? (int)_mSensor.Value : 0;
    }
}