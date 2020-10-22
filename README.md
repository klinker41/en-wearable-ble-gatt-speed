# Exposure Notification Wearable GATT Speed Test

Simple utility for creating a GATT connection between two devices and measuring transfer speeds
depending on the amount of data to transfer and the configured MTU size.

To use, install the app on two phones and open one as client, one as server. On the client,
connect to the server and then use the sliders to configure transfer sizes and the buttons to begin
the transfer. Logs will be outputted giving progress updates.

State management isn't perfect, sometimes the GATT connection doesn't get disconnected properly
between the two devices. If this happens, turn on airplane mode for a few seconds and force close
the app, then they should be able to connect again on the next try.

## Test results

Below are some sample times I collected when using a Pixel 4 as the server and Pixel 4a as the
client.

Test case | MTU | Total bytes transferred | Time taken
--- | --- | --- | ---
Transfer advertisements, 3 days, 144 per day, 20 bytes each | 512 | X | ~2s
Transfer advertisements, 14 days, 288 per day, 31 bytes each | 512 | X | X
Transfer advertisements, 3 days, 144 per day, 20 bytes each | 26 | X | X
Transfer advertisements, 14 days, 288 per day, 31 bytes each | 26 | X | X
