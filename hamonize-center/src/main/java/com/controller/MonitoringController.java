package com.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mapper.IPcMangrMapper;
import com.model.OrgVo;
import com.model.PcDataVo;
import com.model.PcMangrVo;
import com.model.PcMemoryDataVo;
import com.service.MonitoringService;
import com.service.OrgService;

@Controller
@RequestMapping("/mntrng")
public class MonitoringController {

	@Autowired
	private OrgService oService;

	@Autowired
	private MonitoringService mService;

	@Autowired
	private IPcMangrMapper pcmp;

	@RequestMapping(value = "/pcControlList")
	public String pcControlPage(Model model, @RequestParam Map<String, Object> params) {
		JSONArray jsonArray = new JSONArray();
		try {
			OrgVo orgvo = new OrgVo();
			jsonArray = oService.orgList(orgvo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		model.addAttribute("oList", jsonArray);

		return "/mntrng/mntrngList";
	}

	/**
	 * 모니터링 페이지에서 pc 리스트 출력하는 메서드
	 * 
	 * @param model
	 * @param params
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/pcList")
	public Map<String, Object> pcList(Model model, @RequestParam Map<String, Object> params) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> result = new HashMap<String, Object>();
		params.put("org_seq", Integer.parseInt(params.get("org_seq").toString()));
		int on = 0;
		int off = 0;
		try {
			list = mService.pcListInfo(params);
			for (int i = 0; i < list.size(); i++) {

				// if (list.get(i).get("pc_status") != null && list.get(i).get("pc_status") != "" )
				// {
				if (!ObjectUtils.isEmpty(list.get(i).get("pc_status"))) {
					on++;
				} else {
					off++;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("on==={" + on);
		System.out.println("off==={" + off);

		result.put("pcList", list);
		result.put("on", on);
		result.put("off", off);

		model.addAttribute("pcList", list);
		model.addAttribute("on", on);
		model.addAttribute("off", off);
		return result;
	}

	/**
	 * 로그감사 > 정책배포결과 조직도 클릭시 해당 부서/팀에 소속된 pc리스트와 적용된 정책리스트 출력 출력 사항 : 업데이트 배포 결과, 프로그램 차단 배포 결과, 방화벽
	 * 정책 배포결과, 디바이스 정책배포결과
	 * 
	 */
	@ResponseBody
	@RequestMapping(value = "/pcPolicyList")
	public HashMap<String, Object> pcPolicyList(Model model,
			@RequestParam Map<String, Object> params) {
		HashMap<String, Object> jsonObject = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		params.put("org_seq", Integer.parseInt(params.get("org_seq").toString()));
		params.put("type", params.get("type").toString());

		System.out.println("type========" + params.get("type").toString());

		int on = 0;
		int off = 0;

		try {
			list = mService.pcListInfo(params);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).get("pc_status") != null)
					on++;
				else
					off++;
			}

			System.out.println("on====" + on);
			System.out.println("off====" + off);
			result = pcmp.pcPolicyUpdtList(params);
			jsonObject.put("pcList", list);
			jsonObject.put("policyUpdtResult", result);
			jsonObject.put("policyProgrmResult", pcmp.pcPolicyProgrmList(params));
			jsonObject.put("policyFirewallResult", pcmp.pcPolicyFirewallList(params));
			jsonObject.put("policyDeviceResult", pcmp.pcPolicyDeviceList(params));

		} catch (Exception e) {
			e.printStackTrace();
		}
		model.addAttribute("on", on);
		model.addAttribute("off", off);
		return jsonObject;
	}

	/**
	 * 상세 로깅정보 출력 화면
	 * 
	 */
	@RequestMapping(value = "/pcView")
	public String pcInfo(Model model, @RequestParam Map<String, Object> params) {
		String uuid = "";
		PcMangrVo vo = new PcMangrVo();
		if (!params.isEmpty()) {
			uuid = params.get("uuid").toString();
			vo.setPc_uuid(uuid);
			vo = pcmp.pcDetailInfo(vo);
			// System.out.println("vo===="+vo.toString());

		}

		model.addAttribute("uuid", uuid);
		model.addAttribute("pcvo", vo);

		return "/mntrng/mntmgView";
	}

	@ResponseBody
	@RequestMapping(value = "/memoryUsage")
	public JSONArray memoryUsage(Model model, @RequestParam Map<String, Object> params) {
		List<PcMemoryDataVo> list = mService.getMemory(params.get("uuid").toString());
		JSONObject mem;
		JSONArray ja = new JSONArray();
		for (int i = 0; i < list.size(); i++) {
			mem = new JSONObject();
			mem.put("memory", list.get(i).getValue());
			ja.add(mem);
		}
		return ja;
	}

	@ResponseBody
	@RequestMapping(value = "/cpuUsage")
	public JSONArray cpuUsage(Model model, @RequestParam Map<String, Object> params) {
		List<PcDataVo> list = mService.getCpu(params.get("uuid").toString());
		JSONObject cpu;
		JSONArray ja = new JSONArray();
		for (int i = 0; i < list.size(); i++) {
			cpu = new JSONObject();
			System.out.println("cpu====" + list.get(i).getValue());
			cpu.put("cpu", list.get(i).getValue());
			ja.add(cpu);
		}
		return ja;
	}

}
