import { defineStore } from 'pinia';
import { DictState, LabelValueState } from '@/store/modules/dict/types';

const useDictStore = defineStore('dict', {
  state: () => ({ dict: [] as Array<DictState> }),
  actions: {
    // 获取字典
    getDict(_code: string) {
      if (_code == null || _code === '') {
        return null;
      }
      for (let i = 0; i < this.dict.length; i += 1) {
        if (this.dict[i].code === _code) {
          return this.dict[i].items;
        }
      }
      return null;
    },
    // 设置字典
    setDict(_code: string, items: Array<LabelValueState>) {
      if (_code !== null && _code !== '') {
        this.dict.push({
          code: _code,
          items,
        });
      }
    },
    // 删除字典
    deleteDict(_code: string) {
      let bln = false;
      try {
        for (let i = 0; i < this.dict.length; i += 1) {
          if (this.dict[i].code === _code) {
            this.dict.splice(i, 1);
            return true;
          }
        }
      } catch (e) {
        bln = false;
      }
      return bln;
    },
    // 清空字典
    cleanDict() {
      this.dict = [];
    },
  },
});

export default useDictStore;
