import { ref, toRefs } from 'vue';
import { listEnumDict } from '@/api/common';
import { useDictStore } from '@/store';

/**
 * 获取字典数据
 *
 * @param names 字典名列表
 */
export default function useDict(...names: Array<string>) {
  const res = ref<any>({});
  return (() => {
    names.forEach((name: string) => {
      res.value[name] = [];
      const dict = useDictStore().getDict(name);
      if (dict) {
        res.value[name] = dict;
      } else {
        listEnumDict(name).then((resp) => {
          res.value[name] = resp.data;
          useDictStore().setDict(name, res.value[name]);
        });
      }
    });
    return toRefs(res.value);
  })();
}
