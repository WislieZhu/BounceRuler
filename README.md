效果:

https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/8daea57946ef494881246030996df5c1~tplv-k3u1fbpfcp-watermark.image?
思路:

1.结合OverScroller(有回弹效果), GestureDetector.SimpleOnGestureListener
  onScroll方法: 获取到滑动的偏移量offsetX
  onFling方法: 滚动时的扔掷效果
2.在onDraw方法中使用offsetX,动态的改变刻度尺的显示数值和刻度线
3.如何让滑动结束时，中心位置显示的数值向上和向下取整（让刻度尺再移动一小会）
  onTouchEvent方法:up时，如果处于滚动状态->中心位置取整
  Scroller.fling:Scroller.computeScrollOffset()为false->中心位置取整
4.会存在问题:如果是onFling状态,会先up调用,然后调用Scroller.fling，如何避免onFling时,不再调用up中调整中心位置的方法?
引入速率,up时当速率小于最小速率时，才去调用调整中心位置的方法(onScroll); 达到onFling时,速率会大于最小速率，会绕过up的判断方法
