import { ref, toRefs } from 'vue';
import { listEnumDict, listDict } from '@/api/common';
import { useDictStore } from '@/store';

/**
 * 获取字典数据
 *
 * @param dicts 字典列表
 */
export default function useDict(
  ...dicts: Array<{ name: string; isEnum: boolean }>
) {
  const res = ref<any>({});
  return (() => {
    dicts.forEach((d) => {
      const { name } = d;
      res.value[name] = [];
      const dict = useDictStore().getDict(name);
      if (dict) {
        res.value[name] = dict;
      } else if (d.isEnum) {
        listEnumDict(name).then((resp) => {
          res.value[name] = resp.data;
          useDictStore().setDict(name, res.value[name]);
        });
      } else {
        listDict(name).then((resp) => {
          res.value[name] = resp.data;
          useDictStore().setDict(name, res.value[name]);
        });
      }
    });
    return toRefs(res.value);
  })();
}
