import { ref, toRefs } from 'vue';
import { listDict } from '@/api/common';
import { useDictStore } from '@/store';

/**
 * 获取字典数据
 *
 * @param codes 字典编码列表
 */
export default function useDict(...codes: Array<string>) {
  const res = ref<any>({});
  return (() => {
    codes.forEach((code) => {
      res.value[code] = [];
      const dict = useDictStore().getDict(code);
      if (dict) {
        res.value[code] = dict;
      } else {
        listDict(code).then((resp) => {
          res.value[code] = resp.data;
          useDictStore().setDict(code, res.value[code]);
        });
      }
    });
    return toRefs(res.value);
  })();
}
