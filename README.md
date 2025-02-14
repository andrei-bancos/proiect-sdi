# Proiect SDI

### Configurare broker MQTT Mosquitto
#### Config file - broker1.conf
```conf
port 1883
allow_anonymous true

# Configurare bridge către Broker2
connection bridge_to_broker2
address 0.0.0.0:1884

topic # out 2 broker1/
topic broker2/# in 2

cleansession true

```

#### Config file - broker2.conf
```conf
port 1884
allow_anonymous true

# Configurare bridge către Broker1
connection bridge_to_broker1
address 0.0.0.0:1883

topic # out 2 broker2/
topic broker1/# in 2

cleansession true

```
