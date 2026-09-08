this uses the 0cross circuit (as shown in the hardware file) to detect when the ac mains crosses 0 volts 
General pattern:

41520947 → 41540924 = 9977 µs
41540924 → 41560900 = 9976 µs
41560900 → 41580878 = 9978 µs
41580878 → 41600853 = 9975 µs
41600853 → 41620839 = 9986 µs
41620839 → 41640814 = 9975 µs
41640814 → 41660791 = 9977 µs
41660791 → 41680768 = 9977 µs
41680768 → 41700744 = 9976 µs

So im getting about 9.98 ms between detections.
the are centered around 10 ms.

The deviation is typically ±0.03% at most.

That means the timing jitter is very tight — less than 0,05% error relative to the target.