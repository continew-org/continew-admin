import { defineStore } from 'pinia';
import { DictState, LabelValueState } from '@/store/modules/dict/types';

const useDictStore = defineStore('dict', {
  state: () => ({ dict: [] as Array<DictState> }),
  actions: {
    // 获取字典
    getDict(_name: string) {
      if (_name === null && _name === '') {
        return null;
      }
      try {
        for (let i = 0; i < this.dict.length; i += 1) {
          if (this.dict[i].name === _name) {
            return this.dict[i].detail;
          }
        }
      } catch (e) {
        console.log(e);
      }
      return null;
    },
    // 设置字典
    setDict(_name: string, detail: Array<LabelValueState>) {
      if (_name !== null && _name !== '') {
        this.dict.push({
          name: _name,
          detail,
        });
      }
    },
    // 删除字典
    deleteDict(_name: string) {
      let bln = false;
      try {
        for (let i = 0; i < this.dict.length; i += 1) {
          if (this.dict[i].name === _name) {
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
