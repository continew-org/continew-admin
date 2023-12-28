export function resetSize(vm) {
  let img_width;
  let img_height;
  let bar_width;
  let bar_height; // 图片的宽度、高度，移动条的宽度、高度

  const parentWidth = vm.$el.parentNode.offsetWidth || window.offsetWidth;
  const parentHeight = vm.$el.parentNode.offsetHeight || window.offsetHeight;
  if (vm.imgSize.width.indexOf('%') !== -1) {
    img_width = `${(parseInt(vm.imgSize.width, 10) / 100) * parentWidth}px`;
  } else {
    img_width = vm.imgSize.width;
  }

  if (vm.imgSize.height.indexOf('%') !== -1) {
    img_height = `${(parseInt(vm.imgSize.height, 10) / 100) * parentHeight}px`;
  } else {
    img_height = vm.imgSize.height;
  }

  if (vm.barSize.width.indexOf('%') !== -1) {
    bar_width = `${(parseInt(vm.barSize.width, 10) / 100) * parentWidth}px`;
  } else {
    bar_width = vm.barSize.width;
  }

  if (vm.barSize.height.indexOf('%') !== -1) {
    bar_height = `${(parseInt(vm.barSize.height, 10) / 100) * parentHeight}px`;
  } else {
    bar_height = vm.barSize.height;
  }

  return {
    imgWidth: img_width,
    imgHeight: img_height,
    barWidth: bar_width,
    barHeight: bar_height,
  };
}
