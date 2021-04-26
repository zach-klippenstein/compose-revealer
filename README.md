# compose-revealer

A proof-of-concept for a navigational architecture where child screens open with some sort of "reveal" animation from their parent screens. In traditional "shared element transition" design, this is typically implemented by matching certain views in the parent and the new child, and then hiding the views in one while animating the views from the other, so it _looks_ like the elements are shared between the screens.

In this prototype, the navigation is implemented by actually rendering the child screens themselves _in_ the parent. When expanded/revealed, the child screens are responsible for smoothly transitioning from a "collapsed" state into a fully revealed one. When the child screens are fully expanded, the parent can stop being composed to save resources (especially when children are nested multiple layers deep).

Here's an example, replicating bits of a certain app store's UI:

![demo 1](.assets/revealer-demo.gif)
![demo 2](.assets/revealer-demo-video.gif)
